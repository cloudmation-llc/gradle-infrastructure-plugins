package com.cloudmation.gradle.config


import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExpandoConfigDslTests {

    def aws = new ExpandoConfigDsl("aws", this)

    def file(filename, string1, string2) {
        println "Calling file(..) with args ${filename} ${string1} ${string2} on the delegate owner!"
        return null
    }

    @Test
    @Order(1)
    void createAwsConfigFromClosure() {
        // Define closure
        def configClosure = {
            profile = "some-profile"
            region "some-region"

            cloudformation {
                stackPrefix = "hello"
                someFileProperty file("arg1", "arg2", "arg3")

                stacks {
                    stackA {
                        stackName "hello"
                    }

                    stackB {
                        stackName "world"
                    }
                }

                rollbackNotifications {
                    reallyNestedProperty = "helloworld"
                }
            }
        }

        // Apply closure
        aws.applyConfig(configClosure)

        // Inspect properties
        println "${aws}"
    }

}
