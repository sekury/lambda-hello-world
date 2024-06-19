package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
    roleName = "audit_producer-role",
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "Audit", resourceType = ResourceType.DYNAMODB_TABLE)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

    private static final String AUDIT_TABLE = "cmtr-efbf7095-Audit-test";
    private static final String VALUE = "value";
    private static final String NEW_VALUE = "newValue";
    private static final String UPDATED_ATTRIBUTE = "updatedAttribute";
    private static final String OLD_VALUE = "oldValue";
    private static final String KEY = "key";
    private static final String ID = "id";
    private static final String ITEM_KEY = "itemKey";
    private static final String MODIFICATION_TIME = "modificationTime";

    private final Table auditTable;

    public AuditProducer() {
        DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
        this.auditTable = dynamoDB.getTable(AUDIT_TABLE);
    }

    public Void handleRequest(DynamodbEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
                logger.log("Processing event record: " + record);
                StreamRecord streamRecord = record.getDynamodb();
                Map<String, AttributeValue> newImage = streamRecord.getNewImage();

                String id = UUID.randomUUID().toString();
                String itemKey = newImage.get(KEY).getS();
                String modificationTime = streamRecord.getApproximateCreationDateTime().toInstant().toString();

                Item item = new Item();
                item.withPrimaryKey(ID, id);
                item.withString(ITEM_KEY, itemKey);
                item.withString(MODIFICATION_TIME, modificationTime);

                switch (record.getEventName()) {
                    case "INSERT":
                        Map<String, Object> simpleMapValue = ItemUtils.toSimpleMapValue(newImage);
                        item.withMap(NEW_VALUE, simpleMapValue);
                        break;
                    case "MODIFY":
                        Map<String, AttributeValue> oldImage = streamRecord.getOldImage();
                        item.withString(UPDATED_ATTRIBUTE, VALUE);
                        item.withNumber(OLD_VALUE, Integer.parseInt(oldImage.get(VALUE).getN()));
                        item.withNumber(NEW_VALUE, Integer.parseInt(newImage.get(VALUE).getN()));
                        break;
                    default:
                        break;
                }

                auditTable.putItem(item);
            }

            return null;

        } catch (RuntimeException e) {
            logger.log(e.getMessage(), LogLevel.ERROR);
            throw e;
        }
    }
}
