/**
 * Copyright 2020 Cloudmation LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of t he License at
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

import com.cloudmation.gradle.aws.config.AwsConfigDslExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle project plugin which scans each :cloudformation subproject and performs additional project setup work
 * including dynamically generating tasks specifically for working with CloudFormation templates and deployments.
 */
class AwsProjectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Create AWS configuration extension on root project
        def awsConfig = project.extensions.create("aws", AwsConfigDslExtension.class)
        awsConfig.delegateOwner = project
    }

}
