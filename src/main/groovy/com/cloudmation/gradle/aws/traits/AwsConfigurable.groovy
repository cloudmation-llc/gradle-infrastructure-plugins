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
import org.gradle.api.Project
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

/**
 * Groovy trait that adds AWS configuration lookup superpowers to a task.
 */
trait AwsConfigurable {

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

    def withCredentialsProvider(Closure handler) {
        // Lookup a configured region
        def region = lookupAwsProperty { it.aws?.region }

        // Lookup if a credential profile is configured
        def profileLookup = lookupAwsProperty { it.aws?.profile }
        def profileProvider = profileLookup.map { String configuredProfile ->
            ProfileCredentialsProvider
                .builder()
                .profileName(configuredProfile)
                .build()
        }

        // Lookup if we should assume a specific role for credentials
        def assumeRoleArn = lookupAwsProperty { it.aws?.assumeRole } . orElse(null)
        if(assumeRoleArn) {
            // Lookup if MFA is expected as part of the role assumption
            def requestsMfa = lookupAwsProperty { it.aws?.mfa }

            // Create a new STS client builder
            def stsClientBuilder = StsClient.builder()

            // Apply region if configured
            region.ifPresent { String configuredRegion ->
                stsClientBuilder.region(Region.of(configuredRegion))
            }

            // Apply the credentials profile if configured
            profileProvider.ifPresent { AwsCredentialsProvider provider ->
                stsClientBuilder.credentialsProvider(provider)
            }

            // Build the STS client
            def stsClient = stsClientBuilder.build()

            // Construct the assume role request
            def assumeRoleRequestBuilder = AssumeRoleRequest.builder()

            // Add the role ARN to assume
            assumeRoleRequestBuilder.roleArn(assumeRoleArn)

            // If MFA is expected, prompt for the token now
            // (reference: https://mrhaki.blogspot.com/2010/09/gradle-goodness-get-user-input-values.html)
            if (requestsMfa.isPresent()) {
                if (requestsMfa.get()) {
                    def console = System.console()
                    if (console) {
                        def token = console.readLine('> Please enter the 6-digit MFA token: ')
                        assumeRoleRequestBuilder.tokenCode(token)
                    } else {
                        throw new RuntimeException("MFA is requested, but could not get a console instance to read input")
                    }
                }
            }

            // Finish building the assume role request
            def assumeRoleRequest = assumeRoleRequestBuilder.build() as AssumeRoleRequest

            // Send the role assumption request
            def assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest)

            // Execute the provider handler with the session credentials
            handler(assumeRoleResponse.credentials())
        }
    }


}