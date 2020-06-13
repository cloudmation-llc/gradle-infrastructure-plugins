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

package com.cloudmation.gradle.config

import com.cloudmation.gradle.traits.DelegatedOwner

/**
 * Builds on the Groovy expando object to create an object that can automatically create nested instances of
 * itself, or optionally, specialized types of config blocks. Designed specifically for use in Gradle build
 * where closures are heavily used to provide a custom task DSL.
 */
class ExpandoConfigDsl extends Expando implements DelegatedOwner {

    /**
     * Optional name to identify this config block. Useful for debugging.
     */
    protected final String name

    ExpandoConfigDsl() {
        this.name = "root"
    }

    ExpandoConfigDsl(Object owner) {
        this.name = "root"
        this.delegateOwner = owner
    }

    ExpandoConfigDsl(String name) {
        this.name = name
    }

    ExpandoConfigDsl(String name, Object owner) {
        this.name = name
        this.delegateOwner = owner
    }

    /**
     * Apply a closure to this expando.
     * @param configurer
     * @return
     */
    def applyConfig(Closure configurer) {
        // Reconfigure closure with different delegate and owner
        def rehydratedConfigurer = configurer.rehydrate(this, this.delegateOwner, null)
        rehydratedConfigurer.resolveStrategy = Closure.DELEGATE_FIRST
        rehydratedConfigurer.call()
    }

    /**
     * Creates a nested config block of a given custom type. Used for implementing domain specific config blocks.
     * @param blockName
     * @param dslType
     * @return
     */
    def createNestedDsl(String blockName, Class dslType) {
        def configBlock = dslType
            .getConstructor(String.class, Object.class)
            .newInstance(blockName, delegateOwner)

        properties.put(blockName, configBlock)

        return configBlock
    }

    /**
     * Customize Groovy method resolution to create (or fetch) nested config blocks, delegate to an owner object,
     * or set a property on the expando (in that order).
     * @param methodName Method/property name 
     * @param args Method arguments
     * @return A config block, the result of the parent method, or the property value just set.
     */
    def methodMissing(String methodName, args) {
        // Detect if handling a nested config block
        if(args[0] instanceof Closure) {
            def configBlock = properties.computeIfAbsent(methodName, { key ->
                new ExpandoConfigDsl(methodName, this.delegateOwner)
            })
            
            // Apply config closure
            configBlock.applyConfig(args[0])

            // Return the block
            return configBlock
        }

        // Check if the delegate owner will respond to the method
        else if(delegateOwner?.respondsTo(methodName)) {
            // Invoke the owner
            return delegateOwner.invokeMethod(methodName, args)
        }

        // Detect setting a property as a method (many Gradle tasks do this)
        else if(args.length == 1) {
            properties.put(methodName, args[0])
            return args[0]
        }

        // Else throw an error so we can detect strange behavior
        throw new MissingMethodException(methodName, this.class, args)
    }

    /**
     * Fetch missing properties from the underlying expando.
     * @param name The property name
     * @return
     */
    def propertyMissing(String name) {
        // Pass through to the underlying expando properties map
        properties.get(name)
    }

    /**
     * Set missing properties on the underlying expando.
     * @param name The property name
     * @param value The property value
     */
    def propertyMissing(String name, Object value) {
        // Pass through to the underlying expando properties map
        properties.put(name, value)
    }

    @Override
    String toString() {
        return "${properties}"
    }

}
