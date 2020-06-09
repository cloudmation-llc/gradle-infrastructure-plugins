package com.cloudmation.gradle.aws.config

import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@DisplayName("Tests for AWS configuration DSL with static and dynamic features")
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AwsConfigDslExtensionTests {

    AwsConfigDslExtension extension = new AwsConfigDslExtension()

    @Test
    @Order(1)
    @Disabled
    void createDynamicProperty() {
        extension.hello = "world"
    }

    @Test
    @Order(2)
    @Disabled
    void verifyDynamicProperty() {
        assertEquals(extension.hello, "world")
    }

    @Test
    @Order(3)
    @Disabled
    void testMissingPropertyBehavior() {
        assertNotNull(extension.world)
    }

    @Test
    @Order(4)
    void testNestedPropertyBehavior() {
        // Try setting a basic nested property such as role ARN
        extension?.assumeRole?.roleArn = "some:role:arn"
        assertEquals(extension.assumeRole.roleArn, "some:role:arn")

        // Try setting a deeply nested property
        extension?.cloudformation?.rollbackConfiguration?.someSetting = "hello-world"
        assertEquals(extension.cloudformation.rollbackConfiguration.someSetting, "hello-world")
    }

}
