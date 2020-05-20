package com.cloudmation.gradle.aws

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

/**
 * Gradle project plugin which scans each :cloudformation subproject and performs additional project setup work
 * including useful tasks specifically for working with CloudFormation templates.
 */
class AwsProjectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.subprojects { subproject ->
            // Skip ":cloudformation" subproject
            if(subproject.name == "cloudformation") {
                return
            }

            // Parse tokens from project name
            ext { nameTokens = subproject.name.split("[.]") }

            // Get stack name from tokens
            ext { stackName = nameTokens[nameTokens.size() - 1] }

            // Define template file
            ext { templateFile = subproject.file("${stackName}.yml") }

            task("lint", type: Exec) {
                group "aws"
                description "Run cfn-lint to validate a template prior to deployment"
                commandLine "cfn-lint"
                args "-t", templateFile.toString()
            }

            task("deploy", type: CloudformationDeployTask) {
                dependsOn "lint"
            }
        }
    }

}
