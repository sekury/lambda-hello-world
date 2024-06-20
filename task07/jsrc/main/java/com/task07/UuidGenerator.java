package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(targetRule = "uuid_trigger")
public class UuidGenerator implements RequestHandler<Object, Void> {

	private static final String BUCKET_NAME = "cmtr-efbf7095-uuid-storage-test";
	private final AmazonS3 s3Client;
	private final ObjectMapper objectMapper;

	public UuidGenerator() {
		this.s3Client = AmazonS3ClientBuilder.defaultClient();
		this.objectMapper = new ObjectMapper();
	}

	public Void handleRequest(Object event, Context context) {
		Map<String, ?> stringObjectMap = Map.of("ids", getUuids());
		String fileName = Instant.now().toString();
		s3Client.putObject(BUCKET_NAME, fileName, convertObjectToJson(stringObjectMap));
		context.getLogger().log("Uploaded file to S3 bucket: " + fileName);

		return null;
	}

	private String[] getUuids() {
		String[] uuids = new String[10];
		for (int i = 0; i < 10; i++) {
			uuids[i] = UUID.randomUUID().toString();
		}
		return uuids;
	}

	private String convertObjectToJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Object cannot be converted to JSON: " + object);
		}
    }
}
