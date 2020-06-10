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
 * Utilities for working with closures particularly for Gradle task configuration.
 */
class Closures {

    static void extractProperties(Closure targetClosure) {
        // Remap the metaclass for the closure
        targetClosure.metaClass = new ClosureExtensibleMetaClass(targetClosure.metaClass)

        // Extract the property names and values set in the closure metaclass
        def mappedClosure = targetClosure.rehydrate(targetClosure.metaClass, null, null)
        mappedClosure.resolveStrategy = Closure.DELEGATE_ONLY
        mappedClosure.call()
    }

    static void extractProperties(Closure targetClosure, Object delegate) {
        // Remap the metaclass for the closure
        targetClosure.metaClass = new ClosureExtensibleMetaClass(targetClosure.metaClass)

        // Extract the property names and values set in the closure metaclass
        def mappedClosure = targetClosure.rehydrate(
            delegate,
            targetClosure.metaClass,
            null)

        mappedClosure.resolveStrategy = Closure.OWNER_FIRST
        mappedClosure.call()
    }

}
