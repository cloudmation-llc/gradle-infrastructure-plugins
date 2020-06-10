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


package com.cloudmation.gradle.aws.traits

trait PropertiesFileUtilities {

    Properties getPropertiesFile(File propertiesFile) {
        def properties = new Properties()

        // Check if the source file exists, and read it
        // Otherwise an empty properties collection is returned
        if(propertiesFile.exists()) {
            propertiesFile.withReader { properties.load(it) }
        }

        return properties
    }

    def withPropertiesFile(File propertiesFile, Closure handler) {
        // Get existing/create properties collection
        def properties = getPropertiesFile(propertiesFile)

        // Apply the closure to set new properties
        properties.with(handler)

        // Save
        propertiesFile.withWriter { properties.store(it, null) }
    }

}