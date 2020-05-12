package com.cloudmation.gradle.aws

import com.cloudmation.gradle.util.AnsiColors
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

class CloudformationDeployTask extends Exec {

    private boolean doNotDeploy = false
    private boolean doNotExecute = false

    @Option(option = "do-not-deploy", description = "Creates but does not execute the changeset")
    void setDoNotDeploy(boolean value) {
        this.doNotDeploy = value
    }

    @Option(option = "do-not-execute", description = "Runs all steps except executing the deployment command (best for debugging")
    void setDoNotExecute(boolean value) {
        this.doNotExecute = value
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

    @Override
    protected void exec() {
        // Call the local AWS CLI
        commandLine("aws")

        // Add required service call to CloudFormation CLI
        args("cloudformation", "deploy")

        // Add template file
        args("--template-file", project.templateFile)

        // Add stack name
        args("--stack-name", this.getGeneratedStackName())

        // Optionally, add no-execute-changeset
        if(doNotDeploy) {
            logger.lifecycle("Applying --no-execute-changeset to deployment")
            args("--no-execute-changeset")
        }

        // Optionally, set a specific AWS configuration profile
        if(project.hasProperty("awsProfile")) {
            logger.lifecycle("Applying '--profile ${project.awsProfile}' to deployment")
            args("--profile", project.awsProfile)
        }

        // Optionally, set a specific AWS region
        if(project.hasProperty("awsRegion")) {
            logger.lifecycle("Applying '--region ${project.awsRegion}' to deployment")
            args("--region", project.awsRegion)
        }

        // Optionally, set a specific IAM role for CloudFormation to assume on stack execution
        if(project.hasProperty("awsRoleArn")) {
            logger.lifecycle("Applying '--role-arn ${project.awsRoleArn}' to deployment")
            args("--role-arn", project.awsRoleArn)
        }

        // Optionally, set capabilities that may be needed for stack execution
        if(project.hasProperty("awsCapabilities")) {
            logger.lifecycle("Applying '--capabilities ${project.awsCapabilities}' to deployment")
            args("--capabilities", project.awsCapabilities)
        }

        // Determine tags to be added
        def finalTags = new HashMap()
        if(project.rootProject.hasProperty("awsTags")) {
            // Add tags defined in root project
            finalTags.putAll(project.rootProject.awsTags)
        }
        if(project.hasProperty("awsTags")) {
            // Add tags defined in subproject
            finalTags.putAll(project.awsTags)
        }

        // Optionally, add tags to deployment
        if(finalTags.size() > 0) {
            args("--tags")
            finalTags.each { tagKey, tagValue ->
                logger.lifecycle("Applying tag ${tagKey}=${tagValue} to deployment")
                args("${tagKey}=${tagValue}")
            }
        }

        logger.info "Calculated args -> ${args}"

        // Run deployment unless requested not to
        if(!(doNotExecute)) {
            super.exec()
        }
        else {
            logger.lifecycle("${AnsiColors.ORANGE}--do-not-execute is active -- this deployment will not run${AnsiColors.RESET}")
        }
    }
}
