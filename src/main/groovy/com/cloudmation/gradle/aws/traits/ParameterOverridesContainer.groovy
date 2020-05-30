package com.cloudmation.gradle.aws.traits

trait ParameterOverridesContainer {

    Map<String, String> parameterOverrides = new HashMap<>()

    boolean hasParameterOverrides() {
        return parameterOverrides?.size() > 0
    }

    void parameterOverride(String key, String value) {
        parameterOverrides.put(key, value)
    }

}