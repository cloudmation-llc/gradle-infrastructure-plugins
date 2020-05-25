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

package com.cloudmation.gradle.aws

import com.cloudmation.gradle.util.AnsiColors
import software.amazon.awssdk.services.cloudformation.model.DescribeStackEventsRequest
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest
import software.amazon.awssdk.services.cloudformation.model.StackEvent
import software.amazon.awssdk.services.cloudformation.model.StackStatus

import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StackEventsReporter extends CloudformationUtility implements Runnable {

    /**
     * Combination of stack status enums which represent when a stack update
     * is finished regardless of whether the underlying operations were
     * successful.
     */
    private static final EnumSet<StackStatus> stackCompletedConditions = EnumSet.of(
        StackStatus.CREATE_COMPLETE,
        StackStatus.DELETE_COMPLETE,
        StackStatus.ROLLBACK_COMPLETE,
        StackStatus.UPDATE_COMPLETE,
        StackStatus.UPDATE_ROLLBACK_COMPLETE)

    /**
     * Comparator to sort stack events in ascending order by the timestamp.
     */
    private static final Comparator<StackEventWrapper> compareByTimestamp = { o1, o2 ->
        o1.stackEvent.timestamp().compareTo(o2.stackEvent.timestamp())}

    /**
     * Custom formatter to display a basic date and HH:MM:SS timestamp without milliseconds
     * or time zone.
     */
    private static final DateTimeFormatter stackEventTimeFormat = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss")

    private String stackName
    private TreeSet<StackEventWrapper> events = new TreeSet<>(compareByTimestamp)

    StackEventsReporter withStackName(String name) {
        this.stackName = name
        return this
    }

    @Override
    void run() {
        // Create the stack events request
        def describeEventsRequest = DescribeStackEventsRequest
            .builder()
            .stackName(stackName)
            .build()

        // Create the describe stack request
        def describeStacksRequest = DescribeStacksRequest
            .builder()
            .stackName(stackName)
            .build()

        while(true) {
            // Execute request
            def describeEventsResponse = cloudformation.describeStackEvents(describeEventsRequest)

            // Iterate and filter results
            describeEventsResponse
                .stackEvents()
                .stream()
                .filter({ event -> event.clientRequestToken() == this.clientRequestToken })
                .forEach({event -> events.add(new StackEventWrapper(event)) })

            // Log events that have not been output yet
            events
                .stream()
                .filter({ eventWrapper -> (!eventWrapper.printed) })
                .forEach({ eventWrapper ->
                    // Unpack event attributes
                    def logicalId = eventWrapper.stackEvent.logicalResourceId()

                    def physicalId = eventWrapper.stackEvent.physicalResourceId()

                    def reason = eventWrapper.stackEvent.resourceStatusReason()

                    def status = colorizeResourceStatus(eventWrapper.stackEvent.resourceStatusAsString())

                    def zonedTimestamp = eventWrapper.stackEvent
                        .timestamp()
                        .atZone(ZoneId.systemDefault())
                        .format(stackEventTimeFormat)

                    def timestamp = AnsiColors.gray(zonedTimestamp.toString())

                    // Build the log event
                    def logMessage = "${timestamp} - ${status}"

                    // Is the resource event an actual resource, or a stack event?
                    if(logicalId != stackName) {
                        logMessage += ": "

                        if(reason != null) {
                            logMessage += "${reason} "
                        }

                        logMessage += "(logical = ${logicalId}"

                        if(physicalId?.length() > 0) {
                            logMessage += ", physical = ${physicalId}"
                        }

                        logMessage += ")"
                    }

                    // Log
                    logger.lifecycle(logMessage)
                    eventWrapper.printed = true
                })

            // Execute request to get stack info
            def describeStacksResponse = cloudformation.describeStacks(describeStacksRequest)

            // Exit the loop if the stack operation is complete
            if(describeStacksResponse.hasStacks()) {
                def stack = describeStacksResponse.stacks().first()

                if(stackCompletedConditions.contains(stack.stackStatus())) {
                    return
                }
            }

            // Wait before sending next request
            Thread.sleep(1000L)
        }
    }

    /**
     * Applies coloring the resource status of a stack event using similar colors as might be found
     * in the AWS CloudFormation console.
     * @param input The status string to be colorized
     * @return The input padded with ANSI escape codes if the status matches an expected pattern
     */
    private static String colorizeResourceStatus(String input) {
        if(input.contains("FAILED") || input.contains("ROLLBACK")) {
            return AnsiColors.red(input)
        }
        else if(input.contains("IN_PROGRESS")) {
            return AnsiColors.blue(input)
        }
        else if(input.contains("COMPLETE")) {
            return AnsiColors.green(input)
        }
        return input
    }

    /**
     * Inner utility class to track when a stack event has already been logged as to avoid logging
     * an event more than once.
     */
    class StackEventWrapper {

        boolean printed = false
        StackEvent stackEvent

        StackEventWrapper(StackEvent stackEvent) {
            this.stackEvent = stackEvent
        }

    }

}
