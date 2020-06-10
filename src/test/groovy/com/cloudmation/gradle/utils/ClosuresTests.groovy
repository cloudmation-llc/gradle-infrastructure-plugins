package com.cloudmation.gradle.utils

import com.cloudmation.gradle.util.Closures
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClosuresTests {

    def testClosure = {
        stackName = "vpc-zone-a"
        templateFile = file("vpc-zone-network.yml")
        lint = true
        parameterOverride "PrivateSubnetRange", "10.255.1.0/24"
        parameterOverride "PublicSubnetRange", "10.255.251.0/24"
        parameterOverride "RegionAzIndex", "0"
        parameterOverride "ExportSuffix", "ZoneA"
        rollbackNotifications {
            hello "world"
        }
    }

    @Test
    void tryExtractingProperties() {
        Closures.extractProperties(testClosure)
        assertEquals(testClosure.metaClass.lint, true)
    }

}
