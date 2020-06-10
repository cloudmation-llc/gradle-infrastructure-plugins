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


package com.cloudmation.gradle.util

/**
 * Implementation of a Groovy metaclass that supports property extensibility similar to an expando.
 */
class ClosureExtensibleMetaClass extends DelegatingMetaClass {

    private Map<String, Optional<Object>> delegateProperties = new HashMap<>()

    ClosureExtensibleMetaClass(MetaClass delegate) {
        super(delegate)
    }

    ClosureExtensibleMetaClass(Class theClass) {
        super(theClass)
    }

    def methodMissing(String name, def args) {
        // Do nothing
    }

    def propertyMissing(String name) {
        return delegateProperties.get(name)
    }

    def propertyMissing(String name, Object value) {
        // Ignore null properties
        if(value != null) {
            delegateProperties.put(name, value)
        }
    }

    Map<String, Optional<Object>> getDelegateProperties() {
        return delegateProperties
    }

}
