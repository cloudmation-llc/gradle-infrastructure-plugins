package com.cloudmation.gradle.aws

import com.cloudmation.gradle.aws.traits.ParameterOverridesContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

class CloudformationDeployTaskCreationSpec implements ParameterOverridesContainer {

    private final String name
    private Property<String> stackName
    private Property<File> template

    CloudformationDeployTaskCreationSpec(String taskName, ObjectFactory objectFactory) {
        this.name = taskName
        this.stackName = objectFactory.property(String.class)
        this.template = objectFactory.property(File.class)
    }

    String getName() {
        return name
    }

    Property<String> getStackName() {
        return stackName
    }

    void setStackName(String stackName) {
        this.stackName.set(stackName)
    }

    Property<File> getTemplate() {
        return template
    }

    void setTemplate(File template) {
        this.template.set(template)
    }

}
