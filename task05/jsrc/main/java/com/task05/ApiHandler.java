package com.task05;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@LambdaHandler(lambdaName = "api_handler",
    roleName = "api_handler-role",
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(name = "Events", resourceType = ResourceType.DYNAMODB_TABLE)
public class ApiHandler implements RequestHandler<Map<String, Object>, Response> {

    private final DynamoDbTable<Event> eventsTable;

    public ApiHandler() {
        DynamoDbClient standardClient = DynamoDbClient.builder().build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(standardClient).build();
        this.eventsTable = enhancedClient.table("cmtr-efbf7095-Events-test", TableSchema.fromBean(Event.class));
    }

    public Response handleRequest(Map<String, Object> request, Context context) {
        try {
            int principalId = (int) request.get("principalId");
            Map<String, String> content = (Map<String, String>) request.get("content");

            String id = UUID.randomUUID().toString();
            String createdAt = Instant.now().toString();

            Event event = new Event(id, principalId, createdAt, content);

            eventsTable.putItem(event);

            Response response = new Response();
            response.setStatusCode(201);
            response.setEvent(event);
            return response;
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }
}
