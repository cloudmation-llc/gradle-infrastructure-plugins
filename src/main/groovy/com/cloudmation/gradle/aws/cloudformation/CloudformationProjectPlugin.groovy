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

package com.cloudmation.gradle.aws.cloudformation

import com.cloudmation.gradle.aws.config.AwsConfigDsl
import com.cloudmation.gradle.aws.config.TaskGenerationDsl
import org.apache.commons.text.CaseUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild

import static groovy.io.FileType.FILES

/**
 * Gradle project plugin which scans each :cloudformation subprojects and performs additional project setup work
 * including dynamically generating tasks specifically for working with CloudFormation templates and deployments.
 */
class CloudformationProjectPlugin implements Plugin<Project> {

    private static final String DEFAULT_TASK_GROUP = "aws"

    static String camelCase(String input, boolean uppercase = true) {
        return CaseUtils.toCamelCase(
            input,
            uppercase,
            '-' as char,
            '.' as char,
            ' ' as char,
            '_' as char)
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    @Override
    void apply(Project project) {
        project.subprojects { Project subproject ->
            // Skip projects not related to CloudFormation
            if(!(subproject.path.startsWith(":cloudformation"))) {
                return
            }

            // Create AWS configuration extension on subproject
            def projectAwsConfig = subproject.extensions.create("aws", AwsConfigDsl.class)
            projectAwsConfig.delegateOwner = subproject

            // Create a 'cloudformation' config block
            def projectCfConfig = projectAwsConfig.createdNestedDsl("cloudformation", CloudformationConfigDsl.class)

            // Create typed task generation DSL
            def projectTaskGenConfig = projectCfConfig.createdNestedDsl("taskGeneration", TaskGenerationDsl.class)

            // Wait until subproject is completely evaluated
            subproject.afterEvaluate {
                // Check for a custom task group
                def taskGroup = projectTaskGenConfig.group ?: DEFAULT_TASK_GROUP

                // Add a custom method for registering group tasks
                subproject.ext.deployAsGroup = { String taskName, String... tasksToRun ->
                    subproject.tasks.register(taskName, GradleBuild) {
                        group taskGroup
                        tasks = tasksToRun as Collection<String>
                    }
                }

                // Iterate through YAML templates and generate specific tasks
                subproject.projectDir.eachFileMatch(FILES, ~/.*\.(y[a]?ml|json)/, { File template ->
                    def (baseName, extension) = template.name.split("[.]")
                    def camelBaseName = camelCase(baseName)
                    def camelTaskPrefix = camelCase(projectCfConfig.taskPrefix ?: "")
                    def finalBaseName = "${camelTaskPrefix}${camelBaseName}"
                    def pathProject = project.projectDir.toPath()
                    def pathSubproject = subproject.projectDir.toPath()
                    def relativePath = pathProject.relativize(pathSubproject)

                    // Check if lint task should be included based on naming rules
                    def lintTaskName = "lint${finalBaseName}"
                    def lintTaskIncluded  = projectTaskGenConfig.isTaskIncluded(lintTaskName)

                    if(lintTaskIncluded) {
                        subproject.tasks.register(lintTaskName, Exec) {
                            group taskGroup
                            description "Run cfn-lint to validate ${relativePath}/${template.name}"
                            commandLine "cfn-lint"
                            args "-t", template.toString()
                        }
                    }

                    // Check if deploy task should be included based on naming rules
                    def deployTaskName = "deploy${finalBaseName}"
                    def deployTaskIncluded = projectTaskGenConfig.isTaskIncluded(deployTaskName)

                    if(deployTaskIncluded) {
                        subproject.tasks.register (deployTaskName, CloudformationDeployTask) {
                            if(lintTaskIncluded) {
                                dependsOn lintTaskName
                            }
                            group taskGroup
                            templateFile = template
                        }
                    }
                })

                // Iterate through custom stack definitions
                def customStacks = projectCfConfig?.stacks
                customStacks?.each { stackName, creationSpec ->
                    def camelBaseName = camelCase(creationSpec.name)
                    def camelTaskPrefix = camelCase(projectCfConfig.taskPrefix ?: "")
                    def finalBaseName = "${camelTaskPrefix}${camelBaseName}"

                    def specStackName = creationSpec?.stackName
                    def specTemplateFile = creationSpec?.template
                    def specIncludeLintTask = (creationSpec.lint != null) ? creationSpec.lint : true

                    if(specIncludeLintTask) {
                        subproject.tasks.register("lint${finalBaseName}", Exec) {
                            description "Run cfn-lint to validate ${specTemplateFile.name} for custom stack ${specStackName}"
                            group creationSpec.group ?: taskGroup ?: DEFAULT_TASK_GROUP
                            commandLine "cfn-lint"
                            args "-t", specTemplateFile.toString()
                        }
                    }

                    subproject.tasks.register ("deploy${finalBaseName}", CloudformationDeployTask) {
                        if(specIncludeLintTask) {
                            dependsOn "lint${finalBaseName}"
                        }
                        group creationSpec.group ?: taskGroup ?: DEFAULT_TASK_GROUP
                        description "Deploy custom stack ${specStackName}"
                        templateFile = specTemplateFile
                        aws {
                            cloudformation {
                                // Optionally add parameter overrides
                                parameterOverrides = creationSpec.parameterOverrides

                                // Set custom stack name
                                stackName = specStackName
                            }
                        }
                    }
                }
            }
        }
    }

}
