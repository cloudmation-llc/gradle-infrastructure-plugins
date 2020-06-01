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

package com.cloudmation.gradle.aws.traits

trait DynamicTaskProperties {

    private Map<String, Object> propertyOverrides = new HashMap<>()

    def propertyMissing(String key, value) {
        if(value != null) {
            propertyOverrides.put(key, value)
        }
    }

    def propertyMissing(String key) {
        return propertyOverrides.get(key)
    }

}