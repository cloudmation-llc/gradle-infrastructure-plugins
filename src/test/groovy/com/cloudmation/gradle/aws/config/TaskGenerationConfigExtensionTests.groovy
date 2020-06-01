/**
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

package com.cloudmation.gradle.aws.config

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import static org.junit.jupiter.api.Assertions.assertTrue

@DisplayName("Test rules for controlling task generation")
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskGenerationConfigExtensionTests {

    TaskGenerationConfigExtension extension = new TaskGenerationConfigExtension()

    @Test
    @Order(1)
    void createIncludeRuleFromClosure() {
        extension.include({ taskName ->
            return taskName == "lintVpcZoneA"
        })
    }

    @Test
    @Order(1)
    void createIncludeRuleFromString() {
        extension.include(".*VpcZone.*")
    }

    @Test
    @Order(1)
    void createExcludeRuleFromClosure() {
        extension.exclude({ taskName ->
            return taskName == "lintVpcSubnet"
        })
    }

    @Test
    @Order(1)
    void createExcludeRuleFromString() {
        extension.exclude(".*VpcSubnet.*")
    }

    @Test
    @Order(2)
    void checkVpcZoneATaskIncluded() {
        assertTrue(extension.isTaskIncluded("deployVpcZoneA"))
    }

    @Test
    @Order(2)
    void checkVpcZoneDTaskIncluded() {
        assertTrue(extension.isTaskIncluded("lintVpcZoneD"))
    }

    @Test
    @Order(2)
    void checkVpcSubnetTaskIncluded() {
        assertFalse(extension.isTaskIncluded("deployVpcSubnet"))
    }

}
