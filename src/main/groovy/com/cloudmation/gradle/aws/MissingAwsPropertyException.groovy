package com.cloudmation.gradle.aws

class MissingAwsPropertyException extends RuntimeException {

    MissingAwsPropertyException(String propertyName) {
        super("AWS configuration '${propertyName}' must be set at the root project, subproject, or task")
    }

}
