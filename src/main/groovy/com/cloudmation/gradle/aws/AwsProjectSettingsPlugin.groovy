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

import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Gradle settings plugin that scans directories beneath the /templates, and automatically creates CloudFormation
 * subprojects to manage templates and stack deployments.
 */
class AwsProjectSettingsPlugin implements Plugin<Settings> {

    @Override
    void apply(Settings settings) {
        def rootProjectPath = settings.rootDir.toPath()

        new File(settings.rootDir, "cloudformation").eachFileRecurse(FileType.FILES) { file ->
            if(!(file.name.endsWith(".yml"))) {
                // Skip non-YAML files
                return
            }

            def parentPath = file.parentFile.toPath()
            def projectRelativePath = rootProjectPath.relativize(parentPath)
            def projectName = projectRelativePath.getName(projectRelativePath.getNameCount() - 1)
            def generatedProjectName = projectRelativePath
                .toString()
                .replaceAll("[/]", ".")
                .replace("cloudformation.", "cloudformation:")

            // Register subproject
            settings.include generatedProjectName
            settings.project(":${generatedProjectName}").projectDir = file.getParentFile()
            settings.project(":${generatedProjectName}").buildFileName = "${projectName}.gradle"

            // Check (and create) a Gradle build file for the subproject
            def gradleFile = new File("${projectRelativePath}/${projectName}.gradle")
            if(!(gradleFile.exists())) {
                gradleFile.createNewFile()
            }
        }
    }

}
