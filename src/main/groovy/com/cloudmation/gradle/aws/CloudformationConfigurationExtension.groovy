package com.cloudmation.gradle.aws

import software.amazon.awssdk.services.cloudformation.model.CreateChangeSetRequest

class CloudformationConfigurationExtension extends AwsConfigurationExtension {

    private List<Closure> changesetBuilderClosures = new ArrayList<>()

    List<String> capabilities
    Map<String, String> parameterOverrides
    String roleArn

    void capability(String capabilityName) {
        if(capabilities == null) {
            capabilities = new ArrayList<>()
        }

        capabilities.add(capabilityName)
    }

    boolean hasCapabilities() {
        return capabilities?.size() > 0
    }

    boolean hasParameterOverrides() {
        return parameterOverrides?.size() > 0
    }

    void parameterOverride(String key, String value) {
        if(parameterOverrides == null) {
            parameterOverrides = new HashMap<>()
        }

        parameterOverrides.put(key, value)
    }

    void withChangesetBuilder(Closure handler) {
        changesetBuilderClosures.add(handler)
    }

    protected void applyChangesetBuilderClosures(CreateChangeSetRequest.Builder builder) {
        changesetBuilderClosures.each { handler -> handler(builder)}
    }

}
