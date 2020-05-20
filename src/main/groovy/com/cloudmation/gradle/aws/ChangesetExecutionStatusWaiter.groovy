package com.cloudmation.gradle.aws


import software.amazon.awssdk.services.cloudformation.model.DescribeChangeSetRequest
import software.amazon.awssdk.services.cloudformation.model.DescribeChangeSetResponse
import software.amazon.awssdk.services.cloudformation.model.ExecutionStatus

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class ChangesetExecutionStatusWaiter extends CloudformationUtility implements Supplier<Optional<DescribeChangeSetResponse>> {

    private static final EnumSet<ExecutionStatus> errorConditions = EnumSet.of(
        ExecutionStatus.EXECUTE_FAILED,
        ExecutionStatus.OBSOLETE,
        ExecutionStatus.UNAVAILABLE,
        ExecutionStatus.UNKNOWN_TO_SDK_VERSION)

    private static final EnumSet<ExecutionStatus> pendingConditions = EnumSet.of(
        ExecutionStatus.AVAILABLE,
        ExecutionStatus.EXECUTE_IN_PROGRESS)

    private String changesetName
    private ExecutionStatus desiredStatus

    ChangesetExecutionStatusWaiter withChangesetName(String name) {
        this.changesetName = name
        return this
    }

    ChangesetExecutionStatusWaiter withDesiredExecutionStatus(ExecutionStatus status) {
        this.desiredStatus = status
        return this
    }

    void waitFor() {
        logger.debug("Creating future")
        CompletableFuture.supplyAsync(this).get()
        logger.debug("Future execution complete")
    }

    void waitWith(Supplier otherTask) {
        logger.debug("Creating future")
        CompletableFuture
            .anyOf(
                CompletableFuture.supplyAsync(this),
                CompletableFuture.supplyAsync(otherTask))
            .get()
        logger.debug("Future execution complete")
    }

    @Override
    Optional<DescribeChangeSetResponse> get() {
        // Create the request
        def describeRequest = DescribeChangeSetRequest
            .builder()
            .changeSetName(changesetName)
            .build()

        while(true) {
            // Execute request to get changeset info
            def describeResponse = cloudformation.describeChangeSet(describeRequest)

            // Does the changeset exist?
            if(describeResponse.hasChanges()) {
                logger.debug("Changeset execution status = ${describeResponse.executionStatusAsString()}")

                // Does execution status the match what we are looking for?
                if(describeResponse.executionStatus() == desiredStatus) {
                    return Optional.of(describeResponse)
                }

                // Is there an error condition to throw?
                else if(errorConditions.contains(describeResponse.executionStatus())) {
                    throw new ChangesetStatusException("Changeset execution failed: ${describeResponse.statusReason()}")
                }

                // Is the operation still in progress?
                else if(pendingConditions.contains(describeResponse.executionStatus())) {
                    // Wait before checking again
                    Thread.sleep(1000L)
                }

                // Exit the loop
                else {
                    return Optional.empty()
                }
            }
        }
    }

}
