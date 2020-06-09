package com.cloudmation.gradle.config

class ExpandoConfigDsl extends Expando {

    /**
     * Dedicated name field specifically for supporting Gradle task DSL and nested property blocks.
     * @link https://docs.gradle.org/current/javadoc/org/gradle/api/NamedDomainObjectContainer.html
     */
    protected final String name

    ExpandoConfigDsl() {
        this.name = null
    }

    ExpandoConfigDsl(String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    /**
     * Configure missing property behavior to create an expando for dynamically assigning properties. Implementing
     * classes can selectively override behavior for more specific cases.
     * @param key Property name
     * @return Newly created property value
     */
    def propertyMissing(String key) {
        // If no other condition above is met, then default to creating an Expando on the named key
        this[key] = new ExpandoConfigDsl()
        return this[key]
    }

}
