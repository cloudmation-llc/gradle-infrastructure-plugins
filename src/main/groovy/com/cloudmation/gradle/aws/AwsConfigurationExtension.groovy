package com.cloudmation.gradle.aws

class AwsConfigurationExtension {

    String profile
    String region
    Map<String, String> tags

    boolean hasTags() {
        return tags?.size() > 0
    }

    void tag(String key, String value) {
        if(tags == null) {
            tags = new HashMap<>()
        }

        tags.put(key, value)
    }

}
