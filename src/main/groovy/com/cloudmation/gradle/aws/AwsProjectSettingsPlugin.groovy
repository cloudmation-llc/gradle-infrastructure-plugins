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

        new File(settings.rootDir, "templates").eachFileRecurse(FileType.FILES) { file ->
            if(!(file.name.endsWith(".yml"))) {
                // Skip non-YAML files
                return
            }

            def templateName = file.name.replaceAll(".yml", "")
            def parentPath = file.parentFile.toPath()
            def projectRelativePath = rootProjectPath.relativize(parentPath)
            def projectName = projectRelativePath.getName(projectRelativePath.getNameCount() - 1)
            def generatedProjectName = projectRelativePath
                    .toString()
                    .replaceAll("[/]", ".")
                    .replace("templates.", "cloudformation:") + "." + templateName

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
