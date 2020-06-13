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

import com.cloudmation.gradle.aws.AwsBaseTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class CloudformationLintTask extends AwsBaseTask {

    @InputFile File templateFile

    @Override
    def methodMissing(String name, def args) {
        // Ignore missing methods
    }

    @Internal
    @Override
    String getDescription() {
        return "Run cfn-lint to validate ${templateFile.name}"
    }

    @TaskAction
    void run() {
        // Run cfn-lint
        project.exec {
            commandLine "cfn-lint"
            args "-t", templateFile.toString()
        }
    }

}
