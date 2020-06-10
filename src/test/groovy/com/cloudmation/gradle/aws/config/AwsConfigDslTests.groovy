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


package com.cloudmation.gradle.aws.config

import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@DisplayName("Tests for AWS configuration DSL with static and dynamic features")
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AwsConfigDslTests {

    AwsConfigDsl extension = new AwsConfigDsl()

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
