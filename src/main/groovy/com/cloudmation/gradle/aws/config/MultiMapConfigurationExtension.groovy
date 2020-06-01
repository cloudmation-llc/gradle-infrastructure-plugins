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

class MultiMapConfigurationExtension {

    private ArrayListMultimap<String, Object> propertyStorage = ArrayListMultimap.create()

    def methodMissing(String key, def args) {
        propertyStorage.put(key, args[0])
    }

    def propertyMissing(String key, value) {
        if(value != null) {
            propertyStorage.put(key, value)
        }
    }

    @SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
    def propertyMissing(String key) {
        def values = propertyStorage.get(key)

        // Automatically handle cases where the value does not exist, or only one exiss
        if(values.size() == 0) {
            return null
        }
        if(values.size() == 1) {
            return values[0]
        }

        // Return the collection of values
        return values
    }

    def createScope(String name) {
        def extension = extensions.create(name, MultiMapConfigurationExtension.class)
        propertyStorage.put(name, extension)
        return extension
    }

}
