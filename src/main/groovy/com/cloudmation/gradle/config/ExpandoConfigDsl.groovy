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

package com.cloudmation.gradle.config

import com.cloudmation.gradle.traits.DelegatedOwner

class ExpandoConfigDsl extends Expando implements DelegatedOwner {

    /**
     * Optional name to identify this config block. Useful for debugging.
     */
    protected final String name

    ExpandoConfigDsl() {
        this.name = null
    }

    ExpandoConfigDsl(Object owner) {
        this.name = null
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
     * Create a new expando DSL and add it as a named property on this expando.
     * @param key Property name
     * @return The newly created expando instance
     */
    def createdNestedDsl(String key) {
        def expando = new ExpandoConfigDsl(key, delegateOwner)
        getProperties().put(key, expando)
        return expando
    }

    /**
     * Create a new typed DSL object and add it as a named property on this expand.
     * @param key Property name
     * @param dslType Class type to use for constructing the DSL object
     * @return The newly created DSL instance
     */
    def createdNestedDsl(String key, Class dslType) {
        def dslInstance = dslType
            .getConstructor(String.class, Object.class)
            .newInstance(key, delegateOwner)

        getProperties().put(key, dslInstance)

        return dslInstance
    }

    /**
     * Allow the expando properties to be iterated using a closure. Known internal properties such as
     * delegate owner are ignored.
     * @param handler 2 arg closure applied to each entry on the Map.
     * @return
     */
    def each(Closure handler) {
        getProperties().each { key, value ->
            // Filter properties that should not be processed
            if(key != "delegateOwner") {
                handler(key, value)
            }
        }
    }

    /**
     * Configure missing method behavior to resolve properties from the expando if they exist, propagate method
     * calls to a delegated owner if configured, create a nested configuration block if a Gradle closure DSL
     * is provided as an argument, or lastly just set the property on the expando.
     * @param key Property/nested block name
     * @param args List of arguments
     */
    @SuppressWarnings('GroovyAssignabilityCheck')
    def methodMissing(String key, args) {
        // Check if the expando already has the property
        if(getProperties().containsKey(key)) {
            def property = getProperties().get(key)

            // Check if we are attempting to configure an existing expand
            // (could happen if we pre-create DSLs in a project plugin)
            if(property instanceof ExpandoConfigDsl && args[0] instanceof Closure) {
                handleExpandoConfig(property, args[0] as Closure)
                return property
            }
            else {
                return property
            }
        }

        // Check if the owner has the method
        else if(delegateOwner?.metaClass?.respondsTo(key)) {
            // Delegate method call to owner
            return delegateOwner?.invokeMethod(key, args)
        }

        // Is the method call argument a Closure, and therefore likely to be a nested property block?
        else if(args[0] instanceof Closure) {
            // Create the descendent container on the expando properties
            def dslContainer = new ExpandoConfigDsl(key, delegateOwner)

            // Run the closure to configure the container
            handleExpandoConfig(dslContainer, args[0] as Closure)

            // Set the newly created container on the expando
            getProperties().put(key, dslContainer)

            // Return the new container
            return dslContainer
        }

        // Default behavior: set the property on the expando
        getProperties().put(key, args[0])
    }

    /**
     * Helper method to handle configuration closures passed in from Gradle project DSL.
     * @param dslContainer The expando being configured
     * @param configClosure The closure with the configuration logic
     */
    private def handleExpandoConfig(dslContainer, configClosure) {
        def targetClosure

        // If there is a delegate owner, then bind the closure with that
        if(dslContainer.delegateOwner) {
            targetClosure = configClosure.rehydrate(dslContainer, dslContainer.delegateOwner, this)
        }
        else {
            targetClosure = configClosure
            targetClosure.delegate = dslContainer
        }

        // Set the resolve strategy to the expando first, and owner second
        targetClosure.resolveStrategy = Closure.DELEGATE_FIRST

        // Run the config closure
        targetClosure.call()
    }

    /**
     * Configure missing property behavior to create an expando for dynamically assigning properties.
     * @param key Property name
     * @return The property value if resolved on the expando, or a new expando if referencing a new nested property
     */
    def propertyMissing(String key) {
        // Map missing properties onto the expando
        return getProperties().get(key)
    }

    /**
     * Configure missing property behavior where the value is provided to set the property on the expando.
     * @param key Property name
     * @param value Assigned property value
     */
    def propertyMissing(String key, Object value) {
        // Set the property on the expando
        getProperties().put(key, value)
    }

    @Override
    String toString() {
        return "[${name}]: ${this.getProperties()}"
    }

}
