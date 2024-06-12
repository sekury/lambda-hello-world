package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SnsEventSource(
	targetTopic = "task04-sns-topic"
)
@DependsOn(name = "task04-sns-topic", resourceType = ResourceType.SNS_TOPIC)
public class SnsHandler implements RequestHandler<SNSEvent, Boolean> {

	public Boolean handleRequest(SNSEvent event, Context context) {
		LambdaLogger logger = context.getLogger();
		event.getRecords().forEach(snsRecord -> processRecord(snsRecord, logger));
		return true;
	}

	private void processRecord(SNSEvent.SNSRecord snsRecord, LambdaLogger logger) {
		logger.log(snsRecord.getSNS().getMessage());
	}
}
