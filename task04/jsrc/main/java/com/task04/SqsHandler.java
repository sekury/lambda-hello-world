package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import java.util.ArrayList;
import java.util.List;

@LambdaHandler(
    lambdaName = "sqs_handler",
    roleName = "sqs_handler-role",
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(
    targetQueue = "async_queue",
    batchSize = 5
)
@DependsOn(name = "async_queue", resourceType = ResourceType.SQS_QUEUE)
public class SqsHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
        List<SQSBatchResponse.BatchItemFailure> batchItemFailures = new ArrayList<>();
        LambdaLogger logger = context.getLogger();
        for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
            try {
                logger.log(message.getBody());
            } catch (Exception e) {
                batchItemFailures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }
        }
        return new SQSBatchResponse(batchItemFailures);
    }
}
