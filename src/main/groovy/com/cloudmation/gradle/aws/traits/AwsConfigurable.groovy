/**
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

package com.cloudmation.gradle.aws.traits

import com.cloudmation.gradle.aws.config.ConfigScope
import com.cloudmation.gradle.traits.PropertiesFileUtilities
import org.gradle.api.Project
import org.gradle.api.internal.tasks.userinput.UserInputHandler
import org.threeten.extra.AmountFormats
import software.amazon.awssdk.auth.credentials.*
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
 * Groovy trait that adds AWS configuration superpowers to a task.
 */
trait AwsConfigurable implements PropertiesFileUtilities {

    /**
     * Walk the configured scopes (task, project, etc.) to find the first non-null value requested
     * by the provided property access closure.
     * @param propertyAccessor Closure with logic to find a property value
     * @param scopes What config scopes should be considered during the wal
     * @return An optional with either the result, or empty if the value was not found in the tree
     */
    Optional lookupAwsProperty(
        Closure propertyAccessor,
        ConfigScope... scopes = [ConfigScope.SELF, ConfigScope.PROJECT, ConfigScope.PROJECT_TREE]) {

        // Iterate requested scopes
        def result = scopes.findResult { scope ->
            if(scope == ConfigScope.SELF) {
                // Try property lookup on self
                def propertyValue = propertyAccessor(this)
                if(propertyValue != null) {
                    return Optional.of(propertyValue)
                }
            }
            else if(scope == ConfigScope.PROJECT) {
                // Try property lookup on the project
                def propertyValue = propertyAccessor(project)
                if(propertyValue != null) {
                    return Optional.of(propertyValue)
                }
            }
            else if(scope == ConfigScope.PROJECT_TREE && project?.parent) {
                // Try recursive lookup on project hierarchy
                return lookupAwsPropertyInProjectTree(project.parent, propertyAccessor)
            }

            return null
        }

        // Ensure an empty optional is returned if the scope search produced no result
        return (result != null) ? result : Optional.empty()
    }

    /**
     * Recursive helper method to walk the project tree and execute the provided property accessor closure
     * to find the first non-null value. Recursive walk terminates either when a value is found, or when there
     * are no more projects that can be traversed.
     * @param project Gradle project to run the property accessor against
     * @param propertyAccessor Closure with logic to find a property value
     * @return Either an optional with the found value, or null if nothing found
     */
    static Optional lookupAwsPropertyInProjectTree(Project project, Closure propertyAccessor) {
        // Try property lookup on this project
        def propertyValue = propertyAccessor(project)
        if(propertyValue != null) {
            return Optional.of(propertyValue)
        }

        // Can we higher in the tree?
        if(project.parent) {
            return lookupAwsPropertyInProjectTree(project.parent, propertyAccessor)
        }

        return null
    }

    /**
     * Walk the tree from task to root project and return an ordered collection of everything found.
     * @return A List of the found objects.
     */
    List lookupAwsPropertySources() {
        // Create a target list starting with this object
        List<Object> targets = [this]

        // Walk the project tree
        walkProjectTree(project, { targets.add(it) })

        return targets
    }

    /**
     * Recursive helper method to walk the project tree executing the provided handler for each one found, and
     * stopping when there are no more parent projects to traverse.
     * @param start Gradle project to start with
     * @param handler Closure to execute on each project
     */
    static def walkProjectTree(Project start, Closure handler) {
        handler(start)

        // Can we go higher in the project tree?
        if(start.parent) {
            walkProjectTree(start.parent, handler)
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

        // Check to see if a named profile is requested
        def awsProfileName = lookupAwsProperty { it.aws?.profile }.orElse(null)
        if(awsProfileName) {
            def profileFile = ProfileFile.defaultProfileFile()
            def profile = profileFile
                .profile(awsProfileName)
                .orElseThrow({ new RuntimeException("Could not resolved named AWS profile $awsProfileName") })
                .properties()

            // Check if session credentials for the named profile already exist
            Path pathMfaCredentials = Paths.get(System.getProperty("java.io.tmpdir"), ".aws-mfa-session-${awsProfileName}")
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
            if(profile.role_arn && profile.mfa_serial) {
                // Create an AWS STS assume role credentials provider
                def stsAssumeRoleProvider = StsAssumeRoleCredentialsProvider
                    .builder()
                    .stsClient(StsClient.create())
                    .refreshRequest(new StsRequestBuilder(profile, this))
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
                    def expiresInSeconds = Long.parseLong(profile.getOrDefault("duration_seconds", "3600"))
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
                    .profileFile(profileFile)
                    .build())
        }

        // Lastly, enroll the default credentials provider in the chain
        credentialsChainBuilder.addCredentialsProvider(DefaultCredentialsProvider.create())

        // Return credentials provider chain
        return credentialsChainBuilder.build()
    }

    /**
     * Internal helper class to build an AssumeRoleRequest for MFA challenges.
     */
    static protected class StsRequestBuilder implements Consumer<AssumeRoleRequest.Builder> {

        def awsProfile
        def parent

        StsRequestBuilder(Map awsProfile, parent) {
            this.awsProfile = awsProfile
            this.parent = parent
        }

        @SuppressWarnings('GroovyAssignabilityCheck')
        @Override
        void accept(AssumeRoleRequest.Builder builder) {
            // Set role ARN
            builder.roleArn(awsProfile.role_arn)

            // Set role session name (the SDK requires it)
            builder.roleSessionName(awsProfile.role_session_name)

            // Set the MFA device serial number
            builder.serialNumber(awsProfile.mfa_serial)

            // Optionally, set the duration of the session if configured
            if(awsProfile.duration_seconds) {
                builder.durationSeconds(Integer.parseInt(awsProfile.duration_seconds))
            }

            // Prompt the user for the current MFA code
            def userInputService = parent.services.get(UserInputHandler.class) as UserInputHandler
            def mfaCode = userInputService.askQuestion("Please enter the 6-digit MFA token for ${awsProfile.mfa_serial}", "000000")
            builder.tokenCode(mfaCode)
        }

    }

}