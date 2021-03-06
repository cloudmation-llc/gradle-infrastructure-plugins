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

package com.cloudmation.gradle.aws.config


import com.cloudmation.gradle.config.ExpandoConfigDsl
import groovy.transform.InheritConstructors

@InheritConstructors
class AwsConfigDsl extends ExpandoConfigDsl {

    Map<String, Object> tags = new HashMap()

    def tag(String key, String value) {
        tags.put(key, value)
    }

    def tag(String key, Closure executable) {
        tags.put(key, executable)
    }

}
