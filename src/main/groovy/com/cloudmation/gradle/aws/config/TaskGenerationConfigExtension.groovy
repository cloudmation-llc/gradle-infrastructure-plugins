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

package com.cloudmation.gradle.aws.config

import com.google.common.collect.ArrayListMultimap

/**
 * Domain specific extension with properties and utilities for controlling the behavior of plugins that automatically
 * create project tasks according to conventions.
 */
class TaskGenerationConfigExtension {

    private ArrayListMultimap<String, Object> propertyStorage = ArrayListMultimap.create()

    def group

    def exclude(String pattern) {
        propertyStorage.put("excludeRules", { String taskName ->
            return taskName ==~ pattern
        })
    }

    def exclude(Closure handler) {
        propertyStorage.put("excludeRules", handler)
    }

    def isTaskExcluded(taskName) {
        return propertyStorage.get("excludeRules").any { handler -> handler(taskName)}
    }

    def include(String pattern) {
        propertyStorage.put("includeRules", { String taskName ->
            return pattern ==~ taskName
        })
    }

    def include(Closure handler) {
        propertyStorage.put("includeRules", handler)
    }

    def isTaskIncluded(taskName) {
        // Check include rules
        def allowedByRule = propertyStorage.get("includeRules").any { handler -> handler(taskName)}
        if(allowedByRule) {
            return true
        }

        // Check exclude rules
        def excludedByRule = isTaskExcluded(taskName)
        if(excludedByRule) {
            return false
        }

        // Sensible default is allow the task
        return true
    }

}
