package com.cloudmation.gradle.utils


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class MapExperimentTests {

    def mapOfParameters = [
        "param1": "helloworld",
        "param2": { -> "calculated value" },
        "param3": "whatever"]

    @Test
    void tryTransformingMapEntries() {
        println mapOfParameters.collectEntries { key, value ->
            if(value instanceof Closure) {
                return [key, (value as Closure).call()]
            }
            return [key, value]
        }
    }

}
