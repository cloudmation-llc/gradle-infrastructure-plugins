/*
 * Copyright 2020 Cloudmation LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudmation.gradle.aws

import com.cloudmation.gradle.config.ExpandoConfigDsl
import com.cloudmation.gradle.traits.ConfigurableByHierarchy
import com.cloudmation.gradle.traits.PropertiesFileUtilities
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.userinput.UserInputHandler
import org.gradle.api.tasks.Internal
import org.threeten.extra.AmountFormats
import software.amazon.awssdk.auth.credentials.*
import software.amazon.awssdk.profiles.Profile
import software.amazon.awssdk.profiles.ProfileFile
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.util.function.Consumer

/**
 * Base class for building derivative tasks that interact and operate on AWS services. Centralizes some common code
 * that any AWS task will need to function.
 */
class AwsBaseTask extends DefaultTask implements ConfigurableByHierarchy, PropertiesFileUtilities {

    public static final String DEFAULT_TASK_GROUP = "aws"

    @Internal
    ExpandoConfigDsl aws = new ExpandoConfigDsl("aws", project)

    @Internal
    Map<String, Object> propertyOverrides = new HashMap<>()

    def aws(Closure configurer) {
        aws.applyConfig(configurer)
    }

    def methodMissing(String name, def args) {
        // Check if the method being called can be found on the project
        if(project.respondsTo(name)) {
            return project.invokeMethod(name, args)
        }

        return null
    }

    def propertyMissing(String key, value) {
        if(value != null) {
            propertyOverrides.put(key, value)
        }
    }

    def propertyMissing(String key) {
        propertyOverrides.get(key)
    }

    @Internal
    @Override
    String getGroup() {
        if(propertyOverrides.containsKey("group")) {
            return propertyOverrides.get("group")
        }

        // Try to find a group property in the config tree
        return lookupProperty { it.aws?.taskGeneration?.group }.orElse(DEFAULT_TASK_GROUP)
    }

    @Override
    void setGroup(String newGroupName) {
        propertyOverrides.put("group", newGroupName)
    }

    /**
     * Look through the project tree to see if a named profile is specified, and if so, resolve the
     * profile with its properties.
     * @return An Optional describing the resolved profile, or an empty Optional if not found
     */
    Optional<Profile> resolveNamedProfile() {
        // Check to see if a named profile is defined
        return lookupProperty { it.aws?.profile }.map { String profileName ->
            // Attempt to resolve the named profile
            return ProfileFile
                .defaultProfileFile()
                .profile(profileName)
                .orElse(null)
        }
    }

    /**
     * Run through a series of checks to build a chain of one or more credentials providers. Supports named
     * profiles, and if requested, session credentials obtained after a successful MFA challenge. If no profile
     * is specified, then the default credentials provider is used.
     * @return A AwsCredentialsProviderChain with one or more enrolled providers
     */
    @SuppressWarnings('GroovyAssignabilityCheck')
    AwsCredentialsProviderChain resolveCredentialsProvider() {
        // Create an AWS credentials chain builder to combine multiple methods
        def credentialsChainBuilder = AwsCredentialsProviderChain.builder()

        // Lookup named profile
        Optional<Profile> namedProfile = resolveNamedProfile()

        // If present, apply profile to credentials chain
        namedProfile.ifPresent({ Profile profile ->
            def profileName = profile.name()
            def profileProperties = profile.properties()

            logger.lifecycle("Using named profile ${profileName} for credentials")

            // Check if session credentials for the named profile already exist
            Path pathMfaCredentials = Paths.get(System.getProperty("java.io.tmpdir"), ".aws-mfa-session-${profileName}")
            String mfaCredentialsFilename = pathMfaCredentials.getName(pathMfaCredentials.nameCount - 1)

            if (Files.exists(pathMfaCredentials)) {
                def mfaCredentials = getPropertiesFile(pathMfaCredentials)
                if (mfaCredentials.containsKey("accessKeyId")
                    && mfaCredentials.containsKey("secretKeyId")
                    && mfaCredentials.containsKey("sessionToken")
                    && mfaCredentials.containsKey("expires")) {

                    // Check the expiration date
                    LocalDateTime expiration = LocalDateTime.parse(mfaCredentials.getProperty("expires"))
                    LocalDateTime now = LocalDateTime.now()
                    if (expiration.isAfter(now)) {
                        // Add a static credentials provider
                        AwsSessionCredentials sessionCredentials = AwsSessionCredentials
                            .create(
                                mfaCredentials.getProperty("accessKeyId"),
                                mfaCredentials.getProperty("secretKeyId"),
                                mfaCredentials.getProperty("sessionToken"))

                        credentialsChainBuilder.addCredentialsProvider(
                            StaticCredentialsProvider.create(sessionCredentials))

                        if (logger) {
                            // Calculate a helpful duration for when the expiration will happen
                            def timeRemaining = Duration.between(now, expiration)
                            def formattedDuration = AmountFormats.wordBased(timeRemaining, Locale.ENGLISH)

                            logger.lifecycle("Using existing MFA session credentials from ${mfaCredentialsFilename} (expires in ${formattedDuration})")
                        }

                        // Short circuit the credentials resolution and return the chain
                        // with the resolved session credentials
                        return credentialsChainBuilder.build()
                    }
                }
            }

            // Check if a role should be assumed and MFA is expected
            if(profileProperties.role_arn && profileProperties.mfa_serial) {
                // Create an AWS STS assume role credentials provider
                def stsAssumeRoleProvider = StsAssumeRoleCredentialsProvider
                    .builder()
                    .stsClient(StsClient.create())
                    .refreshRequest(new StsRequestBuilder(profileProperties, this))
                    .build()

                // Request session credentials
                AwsSessionCredentials credentials = stsAssumeRoleProvider.resolveCredentials()

                // Write session credentials to local properties file
                withPropertiesFile(pathMfaCredentials, {
                    // Set AWS credential properties
                    setProperty("accessKeyId", credentials.accessKeyId())
                    setProperty("secretKeyId", credentials.secretAccessKey())
                    setProperty("sessionToken", credentials.sessionToken())

                    // Add an expiration timestamp
                    def expiresInSeconds = Long.parseLong(profileProperties.getOrDefault("duration_seconds", "3600"))
                    def expiresTimestamp = LocalDateTime
                        .now()
                        .plusSeconds(expiresInSeconds)

                    setProperty("expires", expiresTimestamp.toString())
                })

                // Add static provider for new session credentials
                credentialsChainBuilder.addCredentialsProvider(
                    StaticCredentialsProvider.create(credentials))

                if(logger) {
                    logger.lifecycle("Wrote new AWS MFA session credentials to ${mfaCredentialsFilename}")
                }
            }

            // Enroll named profile provider next in the credentials chain
            credentialsChainBuilder.addCredentialsProvider(
                ProfileCredentialsProvider
                    .builder()
                    .profileName(profileName)
                    .build())
        })

        // Lastly, enroll the default credentials provider in the chain
        credentialsChainBuilder.addCredentialsProvider(DefaultCredentialsProvider.create())

        // Return credentials provider chain
        return credentialsChainBuilder.build()
    }

    /**
     * Internal helper class to build an AssumeRoleRequest for MFA challenges.
     */
    static protected class StsRequestBuilder implements Consumer<AssumeRoleRequest.Builder> {

        def profileProperties
        def parent

        StsRequestBuilder(Map profileProperties, parent) {
            this.profileProperties = profileProperties
            this.parent = parent
        }

        @SuppressWarnings('GroovyAssignabilityCheck')
        @Override
        void accept(AssumeRoleRequest.Builder builder) {
            // Set role ARN
            builder.roleArn(profileProperties.role_arn)

            // Set role session name (the SDK requires it)
            builder.roleSessionName(profileProperties.role_session_name)

            // Set the MFA device serial number
            builder.serialNumber(profileProperties.mfa_serial)

            // Optionally, set the duration of the session if configured
            if(profileProperties.duration_seconds) {
                builder.durationSeconds(Integer.parseInt(profileProperties.duration_seconds))
            }

            // Prompt the user for the current MFA code
            def userInputService = parent.services.get(UserInputHandler.class) as UserInputHandler
            def mfaCode = userInputService.askQuestion("Please enter the 6-digit MFA token for ${profileProperties.mfa_serial}", "000000")
            builder.tokenCode(mfaCode)
        }

    }

}
