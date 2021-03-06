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

package com.cloudmation.gradle.aws.cloudformation

import com.cloudmation.gradle.aws.AwsBaseTask
import com.cloudmation.gradle.aws.config.ConfigScope
import com.cloudmation.gradle.util.AnsiColors
import com.cloudmation.gradle.util.MapUtilities
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutionException

/**
 * Custom Gradle task which emulates the "cloudformation deploy" operation of the official AWS CLI to deploy
 * CloudFormation stacks from templates. Many conveniences are added here including live reporting of stack
 * events right to the CLI (formerly these would only be visible in the AWS console).
 *
 * Useful features such as stack tagging are easily configurable and allow for the creation of sensible organization
 * defaults to organize stacks and the resources managed by them.
 */
class CloudformationDeployStackTask extends AwsBaseTask {

    protected CloudFormationClient cloudformationClient

    CloudformationDeployStackTask() {
        super()

        // Create the CloudFormation nested block in advance
        aws.createNestedDsl("cloudformation", CloudformationConfigDsl)
    }

    @Internal
    boolean doNotExecute = false

    @Internal
    boolean doNotCreate = false

    @InputFile File templateFile

    @Option(option = "do-not-execute", description = "Creates the stack but does not execute the changeset (see --no-execute-changeset for AWS CLI)")
    void setDoNotExecute(boolean value) {
        this.doNotExecute = value
    }

    @Option(option = "do-not-create", description = "Does not create the stack (primarily for debugging)")
    void setDoNotCreate(boolean value) {
        this.doNotCreate = value
    }

    @Internal
    @Override
    String getDescription() {
        return propertyOverrides.get("description") ?: "Deploy stack ${generatedStackName} from template ${templateFile.name}"
    }

    @Override
    void setDescription(String description) {
        propertyOverrides.put("description", description)
    }

    @Internal
    String getTemplateName() {
        return templateFile.name.split("[.]")[0]
    }

    @Internal
    String getGeneratedStackName() {
        // Check if the task defines a specific stack name
        def customStackName = propertyOverrides.get("stackName")

        // Check if the task or the containing project have an alternate stack prefix configured
        def stackPrefix = lookupProperty(
            { it.aws?.cloudformation?.stackPrefix },
            ConfigScope.SELF, ConfigScope.PROJECT)

        // Compute a final stack name
        def finalStackName = customStackName ?: getTemplateName()

        return stackPrefix
            // Check if the prefix is intentionally an empty string
            .map({ String prefix -> (prefix.length() > 0) ? "${prefix}-${finalStackName}" : finalStackName })

            // By default, generate a stack name from the project name and template name
            .orElse("${project.name}-${finalStackName}")
    }

    @Override
    def methodMissing(String methodName, Object args) {
        // Call the parent class implementation
        def superResult = super.methodMissing(methodName, args)

        // If null, delegate to the CloudFormation config DSL
        return (superResult) ? superResult : aws?.cloudformation?.invokeMethod(methodName, args)
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    @TaskAction
    void deploy() {
        // Create a CloudFormation client builder
        def cloudformationClientBuilder = CloudFormationClient.builder()

        // Optionally, set a default profile for resolving non-credentials configuration
        resolveNamedProfile().ifPresent({ profile ->
            cloudformationClientBuilder.overrideConfiguration({ builder ->
                logger.lifecycle("Using profile ${profile.name()} for deployment")
                builder.defaultProfileName(profile.name())
            })
        })

        // Optionally, set a specific AWS region
        lookupProperty { it.aws?.region } . ifPresent { String region ->
            logger.lifecycle("Using region ${region} for deployment")
            cloudformationClientBuilder.region(Region.of(region))
        }

        // Build the credentials provider chain
        cloudformationClientBuilder.credentialsProvider(resolveCredentialsProvider())

        // Create the CloudFormation client
        this.cloudformationClient = cloudformationClientBuilder.build()

        // Get the local hostname
        def localHostname = InetAddress.getLocalHost().getHostName()

        // Create a timestamp
        def dateNow = LocalDateTime.now()
        def isoTimestamp = dateNow.format(DateTimeFormatter.ISO_DATE_TIME)

        // Generate a stack name
        def generatedStackName = getGeneratedStackName()
        logger.lifecycle("Stack name: ${generatedStackName}")

        // Check if the stack exists to decide on a create or update action
        def stackExists = checkStackExists(generatedStackName)

        // Define a create changeset request builder
        def createChangeSetRequestBuilder = CreateChangeSetRequest.builder()

        // Set deployment type
        def changeSetType = (stackExists) ? ChangeSetType.UPDATE : ChangeSetType.CREATE
        createChangeSetRequestBuilder.changeSetType(changeSetType)
        logger.lifecycle("Changeset type: ${changeSetType.toString()}")

        // Set changeset name
        def changeSetName = "${localHostname}-${changeSetType.toString().toLowerCase()}-${isoTimestamp}".replaceAll("[.:]", "-")
        createChangeSetRequestBuilder.changeSetName(changeSetName)
        logger.lifecycle("Changeset name: ${changeSetName}")

        // Set the stack name
        createChangeSetRequestBuilder.stackName(generatedStackName)

        // Set the template body
        createChangeSetRequestBuilder.templateBody(templateFile.text as String)

        // Optionally, set the IAM role for CloudFormation to assume when executing the stack
        lookupProperty { it.aws?.cloudformation?.roleArn } . ifPresent { String roleArn ->
            logger.lifecycle("Using role ARN ${roleArn} for deployment")
            createChangeSetRequestBuilder.roleARN(roleArn)
        }

        // Optionally, set capabilities (example: IAM) that may be needed for stack execution
        lookupProperty
            { it.aws?.cloudformation?.capabilities }
            .ifPresent { List<String> capabilities ->
                if(capabilities.size() > 0) {
                    logger.lifecycle("Applying capabilities ${capabilities} to deployment")
                    createChangeSetRequestBuilder.capabilitiesWithStrings(capabilities)
                }
            }

        // Optionally, add resource tags merging from root project to task specific
        def tags = lookupPropertySources()
            .reverse()
            .findResults { it.aws?.tags }
            .inject(new HashMap()) { Map result, Map tags ->
                result.putAll(tags.collectEntries(MapUtilities.&transformWithClosures))
                result
            }
            .collect { key, value -> Tag.builder().key(key).value("${value}").build() }

        if(tags.size() > 0) {
            logger.lifecycle("Applying resource tags ${tags} to deployment")
            createChangeSetRequestBuilder.tags(tags)
        }
        else {
            // Apply an empty collection to remove existing tags
            createChangeSetRequestBuilder.tags(new ArrayList<Tag>())
        }

        // Optionally, add parameter overrides with transformations
        def parameterOverrides = lookupPropertySources()
            .reverse()
            .findResults { it.aws?.cloudformation?.parameterOverrides }
            .inject(new HashMap()) { Map result, Map parameters ->
                result.putAll(parameters.collectEntries(MapUtilities.&transformWithClosures))
                result
            }
            .collect { key, value -> Parameter.builder().parameterKey(key).parameterValue("${value}").build() }

        if(parameterOverrides.size() > 0) {
            logger.lifecycle("Applying parameter overrides ${parameterOverrides} to deployment")
            createChangeSetRequestBuilder.parameters(parameterOverrides)
        }

        // Optionally, apply custom changeset builder reconfiguration
        lookupProperty
            { it.aws?.cloudformation?.configureChangeset }
            .ifPresent({ handler -> handler(createChangeSetRequestBuilder) })

        // Complete the build of the changeset request
        def createChangesetRequest = createChangeSetRequestBuilder.build()

        // Check --do-not-create
        if(doNotCreate) {
            // End here
            logger.lifecycle(AnsiColors.yellow("NOTE: the --do-not-create option was specified -- deployment will not run"))
            return
        }

        // Send the create request to propose creating or updating a stack
        logger.lifecycle("Creating changeset...")
        def changesetCreationResponse = cloudformationClient.createChangeSet(createChangesetRequest)
        def changesetArn = changesetCreationResponse.id()

        // Check if the build should fail because the changeset is empty
        def failOnEmptyChangeset = lookupProperty
            { it.aws?.cloudformation?.failOnEmptyChangeset }
            .orElse(false)

        try {
            // Wait for the changeset to be created
            new ChangesetStatusWaiter()
                .withCloudFormationClient(cloudformationClient)
                .withGradleLogger(logger)
                .withChangesetName(changesetArn)
                .withDesiredStatus(ChangeSetStatus.CREATE_COMPLETE)
                .waitFor()
        }
        catch(ExecutionException executionException) {
            def exceptionCause = executionException.getCause()
            if(exceptionCause instanceof EmptyChangesetException && !(failOnEmptyChangeset)) {
                // Do nothing if we are not interested in empty changesets
                logger.lifecycle(AnsiColors.green("No changes needed to be applied"))
                return
            }

            throw exceptionCause
        }

        logger.lifecycle("Successfully created change set ${changeSetName} for stack ${generatedStackName}")

        // Check --do-not-execute
        if(doNotExecute) {
            // End here
            logger.lifecycle(AnsiColors.yellow("NOTE: --do-not-execute option was specified -- the stack was created/updated, but changes were not deployed"))
            logger.lifecycle(AnsiColors.yellow("Use the CloudFormation console to view proposed resource changes"))
            return
        }

        // Execute the changeset to apply the proposed changes
        logger.lifecycle("Applying changeset ${changeSetName}")
        def executeChangesetRequest = ExecuteChangeSetRequest
            .builder()
            .changeSetName(changesetCreationResponse.id())
            .clientRequestToken(changeSetName)
            .build()

        def executeChangesetResponse = cloudformationClient.executeChangeSet(executeChangesetRequest)

        // Wait for the changeset to be executed
        def eventsReporter = new StackEventsReporter()
            .withCloudFormationClient(cloudformationClient)
            .withGradleLogger(logger)
            .withClientRequestToken(changeSetName)
            .withStackName(generatedStackName)

        new ChangesetExecutionStatusWaiter()
            .withCloudFormationClient(cloudformationClient)
            .withGradleLogger(logger)
            .withChangesetName(changesetArn)
            .withDesiredExecutionStatus(ExecutionStatus.EXECUTE_COMPLETE)
            .waitWith(eventsReporter)

        logger.lifecycle(AnsiColors.green("Successfully applied changeset ${changeSetName} for stack ${generatedStackName}"))
    }

    /**
     * Check if the named stack already exists. Special treatment is applied if a stack is in the REVIEW_IN_PROGRESS state.
     * @param stackName Name of the stack
     * @return True if the stack exists
     */
    boolean checkStackExists(String stackName) {
        try {
            def request = DescribeStacksRequest
                .builder()
                .stackName(stackName)
                .build()

            def response = cloudformationClient.describeStacks(request)

            if(response.hasStacks()) {
                if(response.stacks().get(0).stackStatus() == StackStatus.REVIEW_IN_PROGRESS) {
                    return false
                }
                return true
            }
            return false
        }
        catch(CloudFormationException exception) {
            if(exception.awsErrorDetails().errorCode() == "ValidationError" && exception.awsErrorDetails().errorMessage().contains("does not exist")) {
                return false
            }
            throw exception
        }
    }

}
