package com.cloudmation.gradle.aws

import com.cloudmation.gradle.aws.config.AwsConfigDsl
import com.cloudmation.gradle.aws.traits.AwsConfigurable

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

/**
 * Base class for building derivative tasks that interact and operate on AWS services. Centralizes some common code
 * that any AWS task will need to function.
 */
class AwsBaseTask extends DefaultTask implements AwsConfigurable {

    public static final String DEFAULT_TASK_GROUP = "aws"

    @Internal
    Map<String, Object> propertyOverrides = new HashMap<>()

    AwsBaseTask() {
        // Create AWS configuration extension
        def awsConfig = extensions.create("aws", AwsConfigDsl.class)
        awsConfig.delegateOwner = project
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

    /**
     * Expose the AWS configuration extension as a property. Since we create the extension above in the constructor,
     * the Gradle "automagic" to register the extension as a property does not happen unless we do it manually.
     * @return The extension object
     */
    @Internal
    AwsConfigDsl getAws() {
        return extensions.getByName("aws") as AwsConfigDsl
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
