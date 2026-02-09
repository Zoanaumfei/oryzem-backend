package com.oryzem.backend.modules.initiatives.repository;

import com.oryzem.backend.modules.initiatives.domain.Initiative;
import com.oryzem.backend.modules.initiatives.domain.InitiativeKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InitiativeRepository {

    private static final String COUNTER_PK = "COUNTER#INITIATIVE_CODE";
    private static final String COUNTER_SK = "COUNTER";
    private static final String COUNTER_ATTR = "next";
    private static final int CODE_PADDING = 5;
    private static final String IDEMPOTENCY_PK_PREFIX = "IDEMPOTENCY#";
    private static final String IDEMPOTENCY_CREATE_SK = "INITIATIVE_CREATE";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public InitiativeRepository(
            DynamoDbClient dynamoDbClient,
            DynamoDbEnhancedClient enhancedClient,
            @Value("${app.dynamodb.tables.initiatives:Initiatives}") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.enhancedClient = enhancedClient;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    private DynamoDbTable<Initiative> getTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Initiative.class));
    }

    public Initiative save(Initiative initiative) {
        log.info("Saving initiative: {}", initiative.getInitiativeId());
        getTable().putItem(initiative);
        return initiative;
    }

    public Initiative saveIfAbsent(Initiative initiative) {
        Expression condition = Expression.builder()
                .expression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                .expressionNames(Map.of(
                        "#pk", "PK",
                        "#sk", "SK"
                ))
                .build();

        PutItemEnhancedRequest<Initiative> request = PutItemEnhancedRequest.builder(Initiative.class)
                .item(initiative)
                .conditionExpression(condition)
                .build();

        getTable().putItem(request);
        return initiative;
    }

    public Optional<Initiative> findByKey(String pk, String sk) {
        Key key = Key.builder()
                .partitionValue(pk)
                .sortValue(sk)
                .build();
        Initiative initiative = getTable().getItem(key);
        return Optional.ofNullable(initiative);
    }

    public List<Initiative> findAllByYear(String year) {
        String pk = InitiativeKeys.yearPk(year);
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(pk)
                        .build()
        );

        List<Initiative> initiatives = new ArrayList<>();
        for (var page : getTable().query(r -> r.queryConditional(conditional))) {
            initiatives.addAll(page.items());
        }
        return initiatives;
    }

    public List<Initiative> findByInitiativeId(String initiativeId) {
        Expression filter = Expression.builder()
                .expression("#initiativeId = :initiativeId")
                .expressionNames(Map.of("#initiativeId", "initiativeId"))
                .expressionValues(Map.of(
                        ":initiativeId",
                        AttributeValue.builder().s(initiativeId).build()
                ))
                .build();

        List<Initiative> initiatives = new ArrayList<>();
        for (var page : getTable().scan(r -> r.filterExpression(filter))) {
            initiatives.addAll(page.items());
        }
        return initiatives;
    }

    public void delete(String pk, String sk) {
        Key key = Key.builder()
                .partitionValue(pk)
                .sortValue(sk)
                .build();
        getTable().deleteItem(key);
    }

    public String nextInitiativeCode() {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "PK", AttributeValue.builder().s(COUNTER_PK).build(),
                        "SK", AttributeValue.builder().s(COUNTER_SK).build()
                ))
                .updateExpression("SET #val = if_not_exists(#val, :zero) + :inc")
                .expressionAttributeNames(Map.of("#val", COUNTER_ATTR))
                .expressionAttributeValues(Map.of(
                        ":zero", AttributeValue.builder().n("0").build(),
                        ":inc", AttributeValue.builder().n("1").build()
                ))
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();

        var response = dynamoDbClient.updateItem(request);
        String nextValue = response.attributes().get(COUNTER_ATTR).n();
        long numeric = Long.parseLong(nextValue);
        return String.format("INIT-%0" + CODE_PADDING + "d", numeric);
    }

    public Optional<InitiativeIdempotencyRecord> getCreateIdempotency(String key) {
        String pk = IDEMPOTENCY_PK_PREFIX + key;
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "PK", AttributeValue.builder().s(pk).build(),
                        "SK", AttributeValue.builder().s(IDEMPOTENCY_CREATE_SK).build()
                ))
                .build();

        var response = dynamoDbClient.getItem(request);
        if (response.item() == null || response.item().isEmpty()) {
            return Optional.empty();
        }

        Map<String, AttributeValue> item = response.item();
        String initiativeId = item.getOrDefault("initiativeId", AttributeValue.builder().s("").build()).s();
        String initiativeCode = item.getOrDefault("initiativeCode", AttributeValue.builder().s("").build()).s();
        String createdAt = item.getOrDefault("createdAt", AttributeValue.builder().s("").build()).s();
        return Optional.of(new InitiativeIdempotencyRecord(key, initiativeId, initiativeCode, createdAt));
    }

    public boolean putCreateIdempotency(InitiativeIdempotencyRecord record) {
        String pk = IDEMPOTENCY_PK_PREFIX + record.key();
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(Map.of(
                        "PK", AttributeValue.builder().s(pk).build(),
                        "SK", AttributeValue.builder().s(IDEMPOTENCY_CREATE_SK).build(),
                        "initiativeId", AttributeValue.builder().s(record.initiativeId()).build(),
                        "initiativeCode", AttributeValue.builder().s(record.initiativeCode()).build(),
                        "createdAt", AttributeValue.builder().s(record.createdAt()).build()
                ))
                .conditionExpression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                .expressionAttributeNames(Map.of(
                        "#pk", "PK",
                        "#sk", "SK"
                ))
                .build();

        try {
            dynamoDbClient.putItem(request);
            return true;
        } catch (ConditionalCheckFailedException ex) {
            return false;
        }
    }

    public boolean assignInitiativeCodeIfAbsent(String pk, String sk, String initiativeCode) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "PK", AttributeValue.builder().s(pk).build(),
                        "SK", AttributeValue.builder().s(sk).build()
                ))
                .updateExpression("SET initiativeCode = :code")
                .conditionExpression("attribute_exists(PK) AND attribute_exists(SK) AND attribute_not_exists(initiativeCode)")
                .expressionAttributeValues(Map.of(
                        ":code", AttributeValue.builder().s(initiativeCode).build()
                ))
                .build();
        try {
            dynamoDbClient.updateItem(request);
            return true;
        } catch (ConditionalCheckFailedException ex) {
            return false;
        }
    }

    public record InitiativeIdempotencyRecord(
            String key,
            String initiativeId,
            String initiativeCode,
            String createdAt
    ) {
    }
}
