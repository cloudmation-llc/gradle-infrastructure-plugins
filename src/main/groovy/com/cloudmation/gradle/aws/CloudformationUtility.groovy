package com.cloudmation.gradle.aws

import org.gradle.api.logging.Logger
import software.amazon.awssdk.services.cloudformation.CloudFormationClient

abstract class CloudformationUtility<T extends CloudformationUtility> {

    String clientRequestToken
    CloudFormationClient cloudformation
    Logger logger

    T withClientRequestToken(String token) {
        this.clientRequestToken = token
        return this as T
    }

    T withCloudFormationClient(CloudFormationClient client) {
        this.cloudformation = client
        return this as T
    }

    T withGradleLogger(Logger loggerFromTask) {
        this.logger = loggerFromTask
        return this as T
    }

}