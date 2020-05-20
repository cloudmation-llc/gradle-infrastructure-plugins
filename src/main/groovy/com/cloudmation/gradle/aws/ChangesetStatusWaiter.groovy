package com.cloudmation.gradle.aws

import software.amazon.awssdk.services.cloudformation.model.ChangeSetStatus
import software.amazon.awssdk.services.cloudformation.model.DescribeChangeSetRequest
import software.amazon.awssdk.services.cloudformation.model.DescribeChangeSetResponse

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class ChangesetStatusWaiter extends CloudformationUtility implements Supplier<Optional<DescribeChangeSetResponse>> {

    private static final EnumSet<ChangeSetStatus> errorConditions = EnumSet.of(
        ChangeSetStatus.FAILED,
        ChangeSetStatus.UNKNOWN_TO_SDK_VERSION)

    private static final EnumSet<ChangeSetStatus> pendingConditions = EnumSet.of(
        ChangeSetStatus.CREATE_PENDING,
        ChangeSetStatus.CREATE_IN_PROGRESS)

    private String changesetName
    private ChangeSetStatus desiredStatus

    ChangesetStatusWaiter withChangesetName(String name) {
        this.changesetName = name
        return this
    }

    ChangesetStatusWaiter withDesiredStatus(ChangeSetStatus status) {
        this.desiredStatus = status
        return this
    }

    void waitFor() {
        logger.debug("Creating future")
        CompletableFuture.supplyAsync(this).get()
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
                logger.debug("Changeset status = ${describeResponse.statusAsString()}")

                // Does status the match what we are looking for?
                if(describeResponse.status() == desiredStatus) {
                    return Optional.of(describeResponse)
                }

                // Is there an error condition to throw?
                else if(errorConditions.contains(describeResponse.status())) {
                    throw new ChangesetStatusException("Changeset creation failed: ${describeResponse.statusReason()}")
                }

                // Is the operation still in progress?
                else if(pendingConditions.contains(describeResponse.status())) {
                    // Wait before checking again
                    logger.debug("Checking and will wait again")
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
