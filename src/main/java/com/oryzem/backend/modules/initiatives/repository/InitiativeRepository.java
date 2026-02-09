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
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InitiativeRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public InitiativeRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${app.dynamodb.tables.initiatives:Initiatives}") String tableName
    ) {
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
}
