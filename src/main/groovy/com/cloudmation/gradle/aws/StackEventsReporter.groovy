package com.cloudmation.gradle.aws


import software.amazon.awssdk.services.cloudformation.model.DescribeStackEventsRequest
import software.amazon.awssdk.services.cloudformation.model.StackEvent

import java.util.function.Supplier

class StackEventsReporter extends CloudformationUtility implements Supplier {

    private static final Comparator<StackEvent> compareByTimestamp = { o1, o2 ->
        o1.timestamp().compareTo(o2.timestamp())}

    private String stackName
    private TreeSet<StackEvent> events = new TreeSet<>(compareByTimestamp)

    StackEventsReporter withStackName(String name) {
        this.stackName = name
        return this
    }

    @Override
    Object get() {
        // Create the request
        def describeRequest = DescribeStackEventsRequest
            .builder()
            .stackName(stackName)
            .build()

        while(true) {
            // Execute request
            def describeResponse = cloudformation.describeStackEvents(describeRequest)

            // Iterate and filter results
            describeResponse
                .stackEvents()
                .stream()
                .filter({ event -> event.clientRequestToken() == this.clientRequestToken })
                .forEach({event -> events.add(event) })

            // Print events
            logger.lifecycle("${events}")

            // Wait before sending next request
            Thread.sleep(1000L)
        }
    }

}
