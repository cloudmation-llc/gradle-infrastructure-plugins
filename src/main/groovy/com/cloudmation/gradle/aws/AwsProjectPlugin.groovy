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

import com.cloudmation.gradle.aws.cloudformation.CloudformationDeployTask
import com.cloudmation.gradle.aws.cloudformation.CloudformationDeployTaskCreationSpec
import com.cloudmation.gradle.aws.config.MapConfigurationExtension
import org.apache.commons.text.CaseUtils
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild

import static groovy.io.FileType.FILES

/**
 * Gradle project plugin which scans each :cloudformation subproject and performs additional project setup work
 * including dynamically generating tasks specifically for working with CloudFormation templates and deployments.
 */
class AwsProjectPlugin implements Plugin<Project> {

    private static final char[] camelCaseDelimiters = ['.', '-', ' ', '_'] as char[]

    @Override
    void apply(Project project) {
        // Create AWS configuration extension on root project
        project.extensions.create("aws", MapConfigurationExtension.class)

        project.subprojects { Project subproject ->
            // Skip ":cloudformation" subproject
            if(subproject.name == "cloudformation") {
                return
            }

            // Create AWS configuration extension on subproject
            def awsConfigProject = subproject.extensions.create("aws", MapConfigurationExtension.class)
            def cfConfig = awsConfigProject.createScope("cloudformation")

            // Add a CloudFormation custom stack DSL
            NamedDomainObjectContainer<CloudformationDeployTaskCreationSpec> deployTaskCreationContainer =
                subproject.container(CloudformationDeployTaskCreationSpec)

            cfConfig.extensions.add("stacks", deployTaskCreationContainer)

            // Add a custom method for registering group tasks
            subproject.ext.deployAsGroup = { String taskName, String... tasksToRun ->
                subproject.tasks.register(taskName, GradleBuild) {
                    group "aws"
                    tasks = tasksToRun as Collection<String>
                }
            }

            // Wait until subproject is completely evaluated
            subproject.afterEvaluate {
                // Iterate through YAML templates and generate specific tasks
                subproject.projectDir.eachFileMatch(FILES, ~/.*(y[a]?ml)/, { File template ->
                    def (baseName, extension) = template.name.split("[.]")
                    def camelBaseName = CaseUtils.toCamelCase(baseName, true, camelCaseDelimiters)
                    def camelTaskPrefix = CaseUtils.toCamelCase(cfConfig.taskPrefix ?: "", true, camelCaseDelimiters)
                    def finalBaseName = "${camelTaskPrefix}${camelBaseName}"
                    def pathProject = project.projectDir.toPath()
                    def pathSubproject = subproject.projectDir.toPath()
                    def relativePath = pathProject.relativize(pathSubproject)

                    if(cfConfig.taskCreationFilter) {
                        if(!(cfConfig.taskCreationFilter(finalBaseName))) {
                            return
                        }
                    }

                    tasks.register("lint${finalBaseName}", Exec) {
                        group "aws"
                        description "Run cfn-lint to validate ${relativePath}/${template.name}"
                        commandLine "cfn-lint"
                        args "-t", template.toString()
                    }

                    tasks.register ("deploy${finalBaseName}", CloudformationDeployTask) {
                        dependsOn "lint${finalBaseName}"
                        group "aws"
                        description "Deploy stack from template ${relativePath}/${template.name}"
                        stackName = baseName.toLowerCase()
                        templateFile = template
                    }
                })

                // Iterate through custom stack definitions
                def customStacks = cfConfig.extensions.getByName('stacks')
                customStacks.all { creationSpec ->
                    def camelBaseName = CaseUtils.toCamelCase(creationSpec.name, true, camelCaseDelimiters)
                    def camelTaskPrefix = CaseUtils.toCamelCase(cfConfig.taskPrefix ?: "", true, camelCaseDelimiters)
                    def finalBaseName = "${camelTaskPrefix}${camelBaseName}"

                    def specStackName = creationSpec?.stackName?.get()
                    def specTemplateFile = creationSpec?.template?.get()

                    // TODO: Allow override of the group property
                    tasks.register("lint${finalBaseName}", Exec) {
                        group "aws"
                        description "Run cfn-lint to validate ${creationSpec.template.get().name} for custom stack ${specStackName}"
                        commandLine "cfn-lint"
                        args "-t", specTemplateFile.toString()
                    }

                    tasks.register ("deploy${finalBaseName}", CloudformationDeployTask) {
                        dependsOn "lint${finalBaseName}"
                        group "aws"
                        description "Deploy custom stack ${specStackName}"
                        stackName = specStackName
                        templateFile = specTemplateFile

                        // Optionally, add parameter overrides
                        if(creationSpec.hasParameterOverrides()) {
                            parameterOverrides << creationSpec.parameterOverrides
                        }
                    }
                }

                // Create AWS configuration extension on each deploy task
                subproject.tasks.withType(CloudformationDeployTask.class) { task ->
                    // Create AWS configuration extension on subproject
                    def awsConfig = task.extensions.create("aws", MapConfigurationExtension.class)
                    awsConfig.createScope("cloudformation")
                }
            }
        }
    }

}
