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

package com.cloudmation.gradle.aws

import com.cloudmation.gradle.util.AnsiColors
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
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
class CloudformationDeployTask extends DefaultTask {

    protected CloudFormationClient cloudformationClient
    private boolean doNotExecute = false
    private boolean doNotCreate = false

    @Input String stackName
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
    String getGeneratedStackName() {
        // If task a specific stack name configured, use it
        def taskStackName = lookupCloudformationTaskProperty("stackName")
        if(taskStackName) {
            return taskStackName
        }

        // If a stack prefix is specified, use it
        if(project.hasProperty("stackPrefix")) {
            return "${project.stackPrefix}-${project.stackName}"
        }

        // By default, generate a stack prefix from the project name tokens
        def joiner = new StringJoiner("-")
        def nameTokens = project.name.split("[.]")
        for(def index = 0; index < nameTokens.size(); index++) {
            joiner.add(nameTokens[index])
        }

        return joiner
            .add(stackName)
            .toString()
    }

    @Internal Object lookupCloudformationTaskProperty(String propertyName, boolean required = false) {
        def propertyValue = cloudformation?.hasProperty(propertyName) ? cloudformation?.getProperty(propertyName) : null
        if(propertyValue != null) {
            return propertyValue
        }

        // If the property is required and missing, throw an exception to stop execution
        else if(required) {
            throw new MissingAwsPropertyException(propertyName)
        }

        return null
    }

    @Internal
    private Object lookupAwsProperty(String propertyName, boolean required = false) {
        // Look for properties in descending order from most specific sources to least specific

        // First, check the task specific CloudFormation configuration
        def cloudformationTaskValue = lookupCloudformationTaskProperty(propertyName, required)
        if(cloudformationTaskValue != null) {
            return cloudformationTaskValue
        }

        // Second, check the subproject CloudFormation configuration
        def propertyValue = project.cloudformation?.hasProperty(propertyName) ? project.cloudformation?.getProperty(propertyName) : null
        if(propertyValue != null) {
            return propertyValue
        }

        // Third, check the subproject AWS configuration
        propertyValue = project.aws?.hasProperty(propertyName) ? project.aws?.getProperty(propertyName) : null
        if(propertyValue != null) {
            return propertyValue
        }

        // Lastly, check root project AWS configuration
        propertyValue = project.rootProject.aws?.hasProperty(propertyName) ? project.rootProject.aws?.getProperty(propertyName) : null
        if(propertyValue != null) {
            return propertyValue
        }
        // If the property is required and missing, throw an exception to stop execution
        else if(required) {
            throw new MissingAwsPropertyException(propertyName)
        }

        return null
    }

    @Internal
    private void withAwsProperty(String propertyName, boolean required = false, Closure handler) {
        def propertyValue = lookupAwsProperty(propertyName, required)
        propertyValue?.with(handler)
    }

    /**
     * Check for resource tags can be applied to the stack at deployment. Tags are scanned from least specific
     * (i.e. the root project) to the most specific
     * @param handler
     */
    @Internal
    private void withAwsTags(Closure handler) {
        // Look for tags from least specific to most specific. Tags defined at more specific layers
        // can override those from less specific layers.

        def output = new HashMap<String, String>()

        // First, check root project AWS configuration
        if(project.rootProject.aws?.hasTags()) {
            output << project.rootProject.aws.tags
        }

        // Second, check the subproject AWS configuration
        if(project.aws?.hasTags()) {
            output << project.aws.tags
        }

        // Third, check the subproject CloudFormation configuration
        if(project.cloudformation?.hasTags()) {
            output << project.cloudformation.tags
        }

        // Lastly, check the task specific CloudFormation configuration
        if(cloudformation?.hasTags()) {
            output << cloudformation.tags
        }

        // If we captured at least one tag, then call the handler with the collection
        if(output.size() > 0) {
            handler(output)
        }
    }

    /**
     * Check for parameter overrides that can be applied against the template to deploy. Parameters can be defined
     * in the subproject, or per task. Parameters overrides for the task will override those defined at the
     * subproject project.
     * @param handler Called with the parameter collection if at least one parameter is defined
     */
    @Internal
    private void withCloudformationParameters(Closure handler) {
        // Look for tags from least specific to most specific. Tags defined at more specific layers
        // can override those from less specific layers.

        def output = new HashMap<String, String>()

        // First, check the subproject CloudFormation configuration
        if(project.cloudformation?.hasParameterOverrides()) {
            output << project.cloudformation.parameterOverrides
        }

        // Lastly, check the task specific CloudFormation configuration
        if(cloudformation?.hasParameterOverrides()) {
            output << cloudformation.parameterOverrides
        }

        // If we captured at least one tag, then call the handler with the collection
        if(output.size() > 0) {
            handler(output)
        }
    }

    @TaskAction
    void deploy() {
        // Create a CloudFormation client builder
        def cloudformationClientBuilder = CloudFormationClient.builder()

        // Optionally, set a specific AWS region
        withAwsProperty("region") { String region ->
            logger.lifecycle("Using region ${region} for deployment")
            cloudformationClientBuilder.region(Region.of(region))
        }

        // Optionally, use a specific AWS profile
        withAwsProperty("profile") { String profile ->
            logger.lifecycle("Using profile ${profile} for deployment")
            cloudformationClientBuilder.credentialsProvider(
                ProfileCredentialsProvider
                    .builder()
                    .profileName(profile)
                    .build())
        }

        // Create the CloudFormation client
        this.cloudformationClient = cloudformationClientBuilder.build()

        // Get the local hostname
        def localHostname = InetAddress.getLocalHost().getHostName()

        // Create a timestamp
        def dateNow = LocalDateTime.now()
        def isoTimestamp = dateNow.format(DateTimeFormatter.ISO_DATE_TIME)

        // Generate a stack name
        def generatedStackName = getGeneratedStackName()
        logger.lifecycle("Generating stack name -> ${generatedStackName}")

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
        createChangeSetRequestBuilder.stackName(getGeneratedStackName())

        // Set the template body
        createChangeSetRequestBuilder.templateBody(templateFile.text as String)

        // Optionally, set the IAM role for CloudFormation to assume when executing the stack
        withAwsProperty("roleArn") { String roleArn ->
            logger.lifecycle("Using role ARN ${roleArn} for deployment")
            createChangeSetRequestBuilder.roleARN(roleArn)
        }

        // Optionally, set capabilities (example: IAM) that may be needed for stack execution
        withAwsProperty("capabilities") { List<String> capabilities ->
            logger.lifecycle("Applying capabilities ${capabilities} to deployment")
            createChangeSetRequestBuilder.capabilitiesWithStrings(capabilities)
        }

        // Optionally, add tags
        withAwsTags { Map<String, String> tags ->
            logger.lifecycle("Applying tags ${tags} to deployment")
            def mappedTags = tags.collect { key, value -> Tag.builder().key(key).value(value).build() }
            createChangeSetRequestBuilder.tags(mappedTags)
        }

        // Optionally, add parameter overrides
        withCloudformationParameters { Map<String, String> parameters ->
            logger.lifecycle("Applying parameter overrides ${parameters} to deployment")
            def mappedParams = parameters.collect { String key, String value ->
                Parameter.builder().parameterKey(key).parameterValue(value).build()
            }
            createChangeSetRequestBuilder.parameters(mappedParams)
        }

        // Apply changeset builder reconfigurations from project
        project.cloudformation?.applyChangesetBuilderClosures(createChangeSetRequestBuilder)
        cloudformation?.applyChangesetBuilderClosures(createChangeSetRequestBuilder)

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

        // Wait for the changeset to be created
        def failOnEmptyChangeset = lookupAwsProperty("failOnEmptyChangeset") ?: false
        try {
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
     * Check if the named stack already exists. Special treatment is applied if a stack is in the
     * REVIEW_IN_PROGRESS state.
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
