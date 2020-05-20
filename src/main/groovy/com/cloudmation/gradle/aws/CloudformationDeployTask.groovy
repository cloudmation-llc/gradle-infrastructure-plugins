package com.cloudmation.gradle.aws

import com.cloudmation.gradle.util.AnsiColors
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Custom Gradle task which encapsulates the "cloudformation deploy" operation of the official AWS CLI. This task
 * allows you leverage many of the Gradle conveniences such as settings project wide defaults, subproject specific
 * overrides.
 *
 * Useful features such as stack tagging are easily configurable and allow for the creation of sensible organization
 * defaults to organize stacks and the resources managed by them.
 */
class CloudformationDeployTask extends DefaultTask {

    protected CloudFormationClient cloudformation
    private boolean doNotExecute = false
    private boolean doNotCreate = false

    @Option(option = "do-not-execute", description = "Creates the stack but does not execute the changeset (see --no-execute-changeset for AWS CLI)")
    void setDoNotExecute(boolean value) {
        this.doNotExecute = value
    }

    @Option(option = "do-not-create", description = "Does not create the stack (primarily for debugging)")
    void setDoNotCreate(boolean value) {
        this.doNotCreate = value
    }

    @Input
    @Override
    String getGroup() {
        return "aws"
    }

    @Input
    @Override
    String getDescription() {
        return "Deploy a CloudFormation stack using the template"
    }

    @Input
    String getGeneratedStackName() {
        // If a stack prefix is specified, use it
        if(project.hasProperty("stackPrefix")) {
            return "${project.stackPrefix}-${project.stackName}"
        }

        // By default, generate a stack prefix from the project name tokens
        def joiner = new StringJoiner("-")
        for(def index = 0; index < project.nameTokens.size() - 1; index++) {
            joiner.add(project.nameTokens[index])
        }

        return joiner
            .add(project.stackName)
            .toString()
    }

    @TaskAction
    void deploy() {
        // Create a CloudFormation client builder
        def cloudformationClientBuilder = CloudFormationClient.builder()

        // Optionally, set a specific AWS region
        if(project.hasProperty("awsRegion")) {
            logger.lifecycle("Using region ${project.awsRegion} for deployment")
            cloudformationClientBuilder.region(Region.of(project.awsRegion as String))
        }

        // Optionally, use a specific AWS profile
        if(project.hasProperty("awsProfile")) {
            logger.lifecycle("Applying '--profile ${project.awsProfile}' to deployment")
            cloudformationClientBuilder.credentialsProvider(
                ProfileCredentialsProvider
                    .builder()
                    .profileName(project.awsProfile as String)
                    .build())
        }

        // Create the CloudFormation client
        this.cloudformation = cloudformationClientBuilder.build()

        // Check --do-not-create
        if(doNotCreate) {
            // End here
            logger.lifecycle(AnsiColors.yellow("NOTE: the --do-not-create option was specified -- deployment will not run"))
            return
        }

        // Get the local hostname
        def localHostname = InetAddress.getLocalHost().getHostName()

        // Create a timestamp
        def dateNow = LocalDateTime.now()
        def isoTimestamp = dateNow.format(DateTimeFormatter.ISO_DATE_TIME)

        // Generate a stack name
        def generatedStackName = getGeneratedStackName()

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
        createChangeSetRequestBuilder.templateBody(project.templateFile.text as String)

        // Optionally, set the IAM role for CloudFormation to assume when executing the stack
        if(project.hasProperty("awsRoleArn")) {
            logger.lifecycle("Applying role ARN ${project.awsRoleArn} to deployment")
            createChangeSetRequestBuilder.roleARN(project.awsRoleArn as String)
        }

        // Optionally, set capabilities (example: IAM) that may be needed for stack execution
        if(project.hasProperty("awsCapabilities")) {
            logger.lifecycle("Applying capabilities ${project.awsCapabilities} to deployment")
            createChangeSetRequestBuilder.capabilitiesWithStrings(project.awsCapabilities as Collection<String>)
        }

        // Add tags defined in root project
        validatePropertyIsMap(
                project.rootProject.findProperty("awsTags"),
                "'awsTags' in root project")?.each { key, value ->
            def tag = Tag.builder().key(key).value(value).build()
            createChangeSetRequestBuilder.tags(tag)
        }

        // Add tags defined in current project
        validatePropertyIsMap(
                project.findProperty("awsTags"),
                "'awsTags' in current project ${project.name}")?.each { key, value ->
            def tag = Tag.builder().key(key).value(value).build()
            createChangeSetRequestBuilder.tags(tag)
        }

        // Add parameter overrides defined in root project
        validatePropertyIsMap(
                project.rootProject.findProperty("awsParameters"),
                "'awsParameters' in root project")?.each { key, value ->
            def parameter = Parameter.builder().parameterKey(key).parameterValue(value).build()
            createChangeSetRequestBuilder.parameters(parameter)
        }

        // Add parameter overrides defined in current project
        validatePropertyIsMap(
                project.findProperty("awsParameters"),
                "'awsParameters' in current project ${project.name}")?.each { key, value ->
            def parameter = Parameter.builder().parameterKey(key).parameterValue(value).build()
            createChangeSetRequestBuilder.parameters(parameter)
        }

        // Complete the build of the changeset request
        def createChangesetRequest = createChangeSetRequestBuilder.build()

        // Send the create request to propose creating or updating a stack
        logger.lifecycle("Creating changeset...")
        def changesetCreationResponse = cloudformation.createChangeSet(createChangesetRequest)
        def changesetArn = changesetCreationResponse.id()

        // Wait for the changeset to be created
        new ChangesetStatusWaiter()
            .withCloudFormationClient(cloudformation)
            .withGradleLogger(logger)
            .withChangesetName(changesetArn)
            .withDesiredStatus(ChangeSetStatus.CREATE_COMPLETE)
            .waitFor()
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

        def executeChangesetResponse = cloudformation.executeChangeSet(executeChangesetRequest)

        // Wait for the changeset to be executed
        def eventsReporter = new StackEventsReporter()
            .withCloudFormationClient(cloudformation)
            .withGradleLogger(logger)
            .withClientRequestToken(changeSetName)
            .withStackName(generatedStackName)

        new ChangesetExecutionStatusWaiter()
            .withCloudFormationClient(cloudformation)
            .withGradleLogger(logger)
            .withChangesetName(changesetArn)
            .withDesiredExecutionStatus(ExecutionStatus.EXECUTE_COMPLETE)
            .waitWith(eventsReporter)

        logger.lifecycle(AnsiColors.green("Successfully applied changeset ${changeSetName}"))
    }

    /**
     * Check if the named stack already exists.
     * @param stackName
     * @return
     */
    boolean checkStackExists(String stackName) {
        try {
            def request = DescribeStacksRequest
                .builder()
                .stackName(stackName)
                .build()

            def response = cloudformation.describeStacks(request)

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

    /**
     * Checks if a property value is an instance of a java.util.Map. If the value matches, then it is returned.
     * If the value is null/non-existent then nothing happens. If the value type does not match, then an unchecked exception
     * is thrown indicating how to resolve the problem.
     * @param propertyValue The value to be checked
     * @param additionalErrorDetail Context-specific information to include in the exception message
     * @return The original property value
     */
    static Map validatePropertyIsMap(Object propertyValue, String additionalErrorDetail) {
        if(propertyValue == null) {
            return null
        }
        else if(propertyValue instanceof Map) {
            return propertyValue
        }

        throw new RuntimeException("Invalid property type ${(additionalErrorDetail) ? "for ${additionalErrorDetail}" : ""} - expecting a Map instance")
    }

}
