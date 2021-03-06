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

import com.cloudmation.gradle.aws.config.ConfigScope
import org.gradle.api.Project

/**
 * Groovy trait that adds configuration superpowers to a task to lookup configuration properties
 * throughout the entire project tree using closures and recursion.
 */
trait ConfigurableByHierarchy {

    /**
     * Walk the configured scopes (task, project, etc.) to find the first non-null value requested
     * by the provided property access closure.
     * @param propertyAccessor Closure with logic to find a property value
     * @param scopes What config scopes should be considered during the wal
     * @return An optional with either the result, or empty if the value was not found in the tree
     */
    Optional lookupProperty(
        Closure propertyAccessor,
        ConfigScope... scopes = [ConfigScope.SELF, ConfigScope.PROJECT, ConfigScope.PROJECT_TREE]) {

        // Iterate requested scopes
        def result = scopes.findResult { scope ->
            if(scope == ConfigScope.SELF) {
                // Try property lookup on self
                def propertyValue = propertyAccessor(this)
                if(propertyValue != null) {
                    return Optional.of(propertyValue)
                }
            }
            else if(scope == ConfigScope.PROJECT) {
                // Try property lookup on the project
                def propertyValue = propertyAccessor(project)
                if(propertyValue != null) {
                    return Optional.of(propertyValue)
                }
            }
            else if(scope == ConfigScope.PROJECT_TREE && project?.parent) {
                // Try recursive lookup on project hierarchy
                return lookupPropertyInProjectTree(project.parent, propertyAccessor)
            }

            return null
        }

        // Ensure an empty optional is returned if the scope search produced no result
        return (result != null) ? result : Optional.empty()
    }

    /**
     * Recursive helper method to walk the project tree and execute the provided property accessor closure
     * to find the first non-null value. Recursive walk terminates either when a value is found, or when there
     * are no more projects that can be traversed.
     * @param project Gradle project to run the property accessor against
     * @param propertyAccessor Closure with logic to find a property value
     * @return Either an optional with the found value, or null if nothing found
     */
    static Optional lookupPropertyInProjectTree(Project project, Closure propertyAccessor) {
        // Try property lookup on this project
        def propertyValue = propertyAccessor(project)
        if(propertyValue != null) {
            return Optional.of(propertyValue)
        }

        // Can we higher in the tree?
        if(project.parent) {
            return lookupPropertyInProjectTree(project.parent, propertyAccessor)
        }

        return null
    }

    /**
     * Walk the tree from task to root project and return an ordered collection of everything found.
     * @return A List of the found objects.
     */
    List lookupPropertySources() {
        // Create a target list starting with this object
        List<Object> targets = [this]

        // Walk the project tree
        walkProjectTree(project, { targets.add(it) })

        return targets
    }

    /**
     * Recursive helper method to walk the project tree executing the provided handler for each one found, and
     * stopping when there are no more parent projects to traverse.
     * @param start Gradle project to start with
     * @param handler Closure to execute on each project
     */
    static def walkProjectTree(Project start, Closure handler) {
        handler(start)

        // Can we go higher in the project tree?
        if(start.parent) {
            walkProjectTree(start.parent, handler)
        }
    }

}