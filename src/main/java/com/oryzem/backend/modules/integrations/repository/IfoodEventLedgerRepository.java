package com.oryzem.backend.modules.integrations.repository;

import com.oryzem.backend.modules.integrations.domain.IfoodEventLedgerEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.Map;

@Repository
@Slf4j
public class IfoodEventLedgerRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public IfoodEventLedgerRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${app.dynamodb.tables.ifood-event-ledger:}") String tableName
    ) {
        this.enhancedClient = enhancedClient;
        this.tableName = trimToNull(tableName);
    }

    public boolean isConfigured() {
        return tableName != null;
    }

    public boolean saveIfAbsent(IfoodEventLedgerEntry entry) {
        if (!isConfigured()) {
            throw new IllegalStateException("iFood event ledger table is not configured");
        }

        Expression condition = Expression.builder()
                .expression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                .expressionNames(Map.of(
                        "#pk", "merchantId",
                        "#sk", "eventId"
                ))
                .build();

        PutItemEnhancedRequest<IfoodEventLedgerEntry> request = PutItemEnhancedRequest
                .builder(IfoodEventLedgerEntry.class)
                .item(entry)
                .conditionExpression(condition)
                .build();

        try {
            getTable().putItem(request);
            return true;
        } catch (ConditionalCheckFailedException ex) {
            log.debug("iFood event already registered in ledger: merchant={} event={}",
                    entry.getMerchantId(),
                    entry.getEventId());
            return false;
        }
    }

    private DynamoDbTable<IfoodEventLedgerEntry> getTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(IfoodEventLedgerEntry.class));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
