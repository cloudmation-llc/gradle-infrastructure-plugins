package com.cloudmation.gradle.aws

import org.apache.commons.text.CaseUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

import static groovy.io.FileType.FILES

/**
 * Gradle project plugin which scans each :cloudformation subprojects and performs additional project setup work
 * including dynamically generating tasks specifically for working with CloudFormation templates and deployments.
 */
class AwsProjectPlugin implements Plugin<Project> {

    private static final char[] camelCaseDelimiters = ['.', '-', ' '] as char[]

    @Override
    void apply(Project project) {
        // Apply AWS configuration extension to root project
        project.extensions.create("aws", AwsConfigurationExtension.class)

        project.subprojects { Project subproject ->
            // Skip ":cloudformation" subproject
            if(subproject.name == "cloudformation") {
                return
            }

            // Apply task configuration extension to subproject
            def taskConfigExtension = subproject
                .extensions
                .create("taskConfig", TaskConfigurationExtension.class)

            // Apply AWS configuration extension to subproject
            subproject.extensions.create("aws", AwsConfigurationExtension.class)
            subproject.extensions.create("cloudformation", CloudformationConfigurationExtension.class)

            // Wait until subproject is completely evaluated
            subproject.afterEvaluate {
                // Iterate through YAML templates and generate specific tasks
                subproject.projectDir.eachFileMatch(FILES, ~/.*(y[a]?ml)/, { File template ->
                    def (baseName, extension) = template.name.split("[.]")
                    def camelBaseName = CaseUtils.toCamelCase(baseName, true, camelCaseDelimiters)
                    def camelTaskPrefix = CaseUtils.toCamelCase(taskConfigExtension.prefix ?: "", true, camelCaseDelimiters)
                    def finalBaseName = "${camelTaskPrefix}${camelBaseName}"
                    def pathProject = project.projectDir.toPath()
                    def pathSubproject = subproject.projectDir.toPath()
                    def relativePath = pathProject.relativize(pathSubproject)

                    task("lint${finalBaseName}", type: Exec) {
                        group "aws"
                        description "Run cfn-lint to validate ${relativePath}/${template.name}"
                        commandLine "cfn-lint"
                        args "-t", template.toString()
                    }

                    task("deploy${finalBaseName}", type: CloudformationDeployTask) {
                        dependsOn "lint${finalBaseName}"
                        group "aws"
                        description "Deploy stack from template ${relativePath}/${template.name}"
                        stackName = baseName.toLowerCase()
                        templateFile = template
                    }
                })

                // Apply AWS configuration extension to each deploy task
                subproject.tasks.withType(CloudformationDeployTask.class) { task ->
                    task.extensions.create("cloudformation", CloudformationConfigurationExtension.class)
                }
            }
        }
    }

}
