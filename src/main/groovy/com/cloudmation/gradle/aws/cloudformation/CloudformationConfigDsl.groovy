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


import com.cloudmation.gradle.config.ExpandoConfigDsl
import groovy.transform.InheritConstructors

@InheritConstructors
class CloudformationConfigDsl extends ExpandoConfigDsl {

    Map<String, Closure> customStacks = new HashMap<>()
    Map<String, Object> parameterOverrides = new HashMap()

    def parameterOverrides(Map<String, String> params) {
        if(params != null) {
            parameterOverrides.putAll(params)
        }
    }

    def parameterOverride(String key, String value) {
        parameterOverrides.put(key, value)
    }

    def parameterOverride(String key, Closure executable) {
        parameterOverrides.put(key, executable)
    }

    void stack(String stackName, Closure configClosure) {
        customStacks.put(stackName, configClosure)
    }

}
