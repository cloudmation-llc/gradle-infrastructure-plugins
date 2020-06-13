package com.cloudmation.gradle.aws

import com.cloudmation.gradle.aws.traits.AwsConfigurable
import com.cloudmation.gradle.config.ExpandoConfigDsl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

/**
 * Base class for building derivative tasks that interact and operate on AWS services. Centralizes some common code
 * that any AWS task will need to function.
 */
class AwsBaseTask extends DefaultTask implements AwsConfigurable {

    public static final String DEFAULT_TASK_GROUP = "aws"

    @Internal
    ExpandoConfigDsl aws = new ExpandoConfigDsl("aws", project)

    @Internal
    Map<String, Object> propertyOverrides = new HashMap<>()

    def aws(Closure configurer) {
        aws.applyConfig(configurer)
    }

    def methodMissing(String name, def args) {
        // Check if the method being called can be found on the project
        if(project.respondsTo(name)) {
            return project.invokeMethod(name, args)
        }

        return null
    }

    def propertyMissing(String key, value) {
        if(value != null) {
            propertyOverrides.put(key, value)
        }
    }

    def propertyMissing(String key) {
        propertyOverrides.get(key)
    }

    @Internal
    @Override
    String getGroup() {
        if(propertyOverrides.containsKey("group")) {
            return propertyOverrides.get("group")
        }

        // Try to find a group property in the config tree
        return lookupAwsProperty { it.aws?.taskGeneration?.group }.orElse(DEFAULT_TASK_GROUP)
    }

    @Override
    void setGroup(String newGroupName) {
        propertyOverrides.put("group", newGroupName)
    }

}
