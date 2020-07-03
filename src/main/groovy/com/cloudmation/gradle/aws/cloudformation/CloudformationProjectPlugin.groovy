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

import com.cloudmation.gradle.aws.config.AwsConfigDsl
import com.cloudmation.gradle.aws.config.TaskGenerationDsl
import com.cloudmation.gradle.util.Closures
import org.apache.commons.text.CaseUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
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
        // Create a 'cloudformation' config block on the root project
        def awsRootConfig = project.extensions.getByName("aws")
        awsRootConfig.createNestedDsl("cloudformation", CloudformationConfigDsl.class)

        project.subprojects { Project subproject ->
            // Skip projects not related to CloudFormation
            if(!(subproject.path.startsWith(":cloudformation"))) {
                return
            }

            // Create AWS configuration extension on subproject
            def awsConfig = subproject.extensions.create("aws", AwsConfigDsl.class)
            awsConfig.delegateOwner = subproject

            // Create typed task generation DSL
            def taskGenConfig = awsConfig.createNestedDsl("taskGeneration", TaskGenerationDsl.class)

            // Create a 'cloudformation' config block
            def cfConfig = awsConfig.createNestedDsl("cloudformation", CloudformationConfigDsl.class)

            // Wait until subproject is completely evaluated
            subproject.afterEvaluate {
                // Check for a custom task group
                def taskGroup = taskGenConfig.group ?: DEFAULT_TASK_GROUP

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
                    def camelTaskPrefix = camelCase(taskGenConfig.taskPrefix ?: "")
                    def finalBaseName = "${camelTaskPrefix}${camelBaseName}"

                    // Check if lint task should be included based on naming rules
                    def lintTaskName = "lint${finalBaseName}"
                    def lintTaskIncluded  = taskGenConfig.isTaskIncluded(lintTaskName)

                    if(lintTaskIncluded) {
                        subproject.tasks.register(lintTaskName, CloudformationLintTask) {
                            templateFile = template
                        }
                    }

                    // Check if deploy task should be included based on naming rules
                    def deployTaskName = "deploy${finalBaseName}"
                    def deployTaskIncluded = taskGenConfig.isTaskIncluded(deployTaskName)

                    if(deployTaskIncluded) {
                        subproject.tasks.register (deployTaskName, CloudformationDeployStackTask) {
                            if(lintTaskIncluded) {
                                dependsOn lintTaskName
                            }
                            templateFile = template
                        }
                    }
                })

                // Iterate through custom stack definitions
                cfConfig.customStacks.each { String stackName, Closure configurer ->
                    def camelBaseName = camelCase(stackName)
                    def camelTaskPrefix = camelCase(cfConfig.taskPrefix ?: "")
                    def finalBaseName = "${camelTaskPrefix}${camelBaseName}"

                    // Extract the properties from the configurer closure
                    Closures.extractProperties(configurer, subproject)

                    // Create lint task (if enabled)
                    def lintTaskName = "lint${finalBaseName}"
                    if(configurer.metaClass.lint ?: true) {
                        subproject.tasks.register(lintTaskName, CloudformationLintTask) { task ->
                            // Map the configurer closure onto the task
                            def remappedClosure = configurer.rehydrate(task, subproject, null)
                            remappedClosure.resolveStrategy = DELEGATE_FIRST
                            remappedClosure()
                        }
                    }

                    // Create the deploy task
                    subproject.tasks.register(
                        "deploy${finalBaseName}",
                        CloudformationDeployStackTask) { task ->

                        // Map the configurer closure onto the task
                        def remappedClosure = configurer.rehydrate(task, subproject, null)
                        remappedClosure.resolveStrategy = DELEGATE_FIRST
                        remappedClosure()

                        // Add dependencies
                        if(configurer.metaClass.lint ?: true) {
                            dependsOn lintTaskName
                        }
                    }
                }
            }
        }
    }

}
