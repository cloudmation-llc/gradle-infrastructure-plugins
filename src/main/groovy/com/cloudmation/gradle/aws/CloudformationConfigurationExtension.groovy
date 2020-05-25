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

import software.amazon.awssdk.services.cloudformation.model.CreateChangeSetRequest

class CloudformationConfigurationExtension extends AwsConfigurationExtension {

    private List<Closure> changesetBuilderClosures = new ArrayList<>()

    List<String> capabilities
    Map<String, String> parameterOverrides
    String roleArn

    void capability(String capabilityName) {
        if(capabilities == null) {
            capabilities = new ArrayList<>()
        }

        capabilities.add(capabilityName)
    }

    boolean hasCapabilities() {
        return capabilities?.size() > 0
    }

    boolean hasParameterOverrides() {
        return parameterOverrides?.size() > 0
    }

    void parameterOverride(String key, String value) {
        if(parameterOverrides == null) {
            parameterOverrides = new HashMap<>()
        }

        parameterOverrides.put(key, value)
    }

    void withChangesetBuilder(Closure handler) {
        changesetBuilderClosures.add(handler)
    }

    protected void applyChangesetBuilderClosures(CreateChangeSetRequest.Builder builder) {
        changesetBuilderClosures.each { handler -> handler(builder)}
    }

}
