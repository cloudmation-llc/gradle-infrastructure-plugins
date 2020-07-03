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

import com.cloudmation.gradle.ProjectTreeSettingsPlugin
import groovy.io.FileType
import org.gradle.api.initialization.Settings

/**
 * Gradle settings plugin that scans directories beneath the src/cloudformation, and automatically creates CloudFormation
 * subprojects to manage templates and stack deployments.
 */
class CloudformationGradleSettingsPlugin extends ProjectTreeSettingsPlugin {

    @Override
    String getNamespace() {
        return "cloudformation"
    }

    @Override
    void apply(Settings settings) {
        def rootProjectPath = settings.rootDir.toPath()
        def cloudformationDir = new File(settings.rootDir, "src/${namespace}")

        // Check if the 'src/cloudformation' directory exists, or stop if not found
        if(!(cloudformationDir.exists())) {
            // Create src/cloudformation
            cloudformationDir.mkdirs()
        }

        // Configure :cloudformation top-level project
        def topLevelGradleFilename = "${namespace}.gradle"
        settings.include ":${namespace}"
        settings.project(":${namespace}").projectDir = cloudformationDir
        settings.project(":${namespace}").buildFileName = topLevelGradleFilename

        // Check (and create) a Gradle build file for the top level project
        def topLevelGradleFile = new File(cloudformationDir, topLevelGradleFilename)
        if(!(topLevelGradleFile.exists())) {
            topLevelGradleFile.createNewFile()
        }

        // Traverse subdirectories beneath src/cloudformation
        cloudformationDir.eachFileRecurse(FileType.DIRECTORIES) { projectDir ->
            // Skip . directories
            if(projectDir.name[0] == ".") return

            // Skip hidden directories
            if(projectDir.hidden) return

            // Use paths to generate a hierarchical project name
            def projectPath = projectDir.toPath()
            def projectRelativePath = rootProjectPath.relativize(projectPath)
            def projectName = projectDir.name
            def generatedProjectName = projectRelativePath
                .toString()
                .replaceAll("src/", "")
                .replaceAll("[/]", ":")
            def gradleProjectName = ":${generatedProjectName}"

            // Register the subproject
            settings.include generatedProjectName
            settings.project(gradleProjectName).projectDir = projectDir

            // Optionally, allow the use of a named Gradle file instead of the default build.gradle
            def customGradleFilename = "${projectName}.gradle"
            def gradleFile = new File(projectDir, customGradleFilename)
            if(gradleFile.exists()) {
                settings.project(gradleProjectName).buildFileName = customGradleFilename
            }
        }
    }

}
