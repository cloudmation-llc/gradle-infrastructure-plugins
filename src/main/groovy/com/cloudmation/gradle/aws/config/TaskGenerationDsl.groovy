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

import com.cloudmation.gradle.config.ExpandoConfigDsl
import groovy.transform.InheritConstructors

/**
 * Domain specific extension with properties and utilities for controlling the behavior of plugins that automatically
 * create project tasks according to conventions.
 */
@InheritConstructors
class TaskGenerationDsl extends ExpandoConfigDsl {

    @SuppressWarnings('GroovyUncheckedAssignmentOfMemberOfRawType')
    private List<Closure> getRuleList(key) {
        return properties.computeIfAbsent(key, { new ArrayList<Closure>() })
    }

    def exclude(String pattern) {
        getRuleList("excludeRules").add({ String taskName ->
            return taskName ==~ pattern
        })
    }

    def exclude(Closure handler) {
        getRuleList("excludeRules").add(handler)
    }

    def isTaskExcluded(taskName) {
        return getRuleList("excludeRules").any { handler -> handler(taskName)}
    }

    def include(String pattern) {
        getRuleList("includeRules").add({ String taskName ->
            return taskName ==~ pattern
        })
    }

    def include(Closure handler) {
        getRuleList("includeRules").add(handler)
    }

    def isTaskIncluded(taskName) {
        // Check include rules
        def allowedByRule = getRuleList("includeRules").any { handler -> handler(taskName)}
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
