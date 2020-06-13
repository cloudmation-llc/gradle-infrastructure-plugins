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

package com.cloudmation.gradle.traits

import java.nio.file.Files
import java.nio.file.Path

trait PropertiesFileUtilities {

    Properties getPropertiesFile(Path path) {
        def props = new Properties()

        // Check if the source file exists, and read it
        // (otherwise an empty properties collection is returned)
        if(Files.exists(path)) {
            Files.newBufferedReader(path).withCloseable {
                props.load(it)
            }
        }

        return props
    }

    def withPropertiesFile(Path path, Closure handler) {
        // Get existing/create properties collection
        def props = getPropertiesFile(path)

        // Apply the closure to set new properties
        props.with(handler)

        // Save
        Files.newBufferedWriter(path).withCloseable { props.store(it, null) }
    }

}