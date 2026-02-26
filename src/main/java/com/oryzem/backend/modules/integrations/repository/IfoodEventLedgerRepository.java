package com.oryzem.backend.modules.integrations.repository;

import com.oryzem.backend.modules.integrations.domain.IfoodEventLedgerEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
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

    public RegistrationOutcome registerForProcessing(IfoodEventLedgerEntry entry) {
        if (!isConfigured()) {
            throw new IllegalStateException("iFood event ledger table is not configured");
        }

        IfoodEventLedgerEntry receivedEntry = toReceivedEntry(entry);
        PutItemEnhancedRequest<IfoodEventLedgerEntry> request = PutItemEnhancedRequest
                .builder(IfoodEventLedgerEntry.class)
                .item(receivedEntry)
                .conditionExpression(buildAbsentCondition())
                .build();

        try {
            getTable().putItem(request);
            return RegistrationOutcome.ACQUIRED;
        } catch (ConditionalCheckFailedException ex) {
            IfoodEventLedgerEntry existing = findById(entry.getMerchantId(), entry.getEventId());
            if (existing == null) {
                log.warn("iFood ledger race detected for merchant={} event={}. Processing once as fallback.",
                        entry.getMerchantId(),
                        entry.getEventId());
                return RegistrationOutcome.ACQUIRED;
            }

            String currentStatus = normalizeStatus(existing.getStatus());
            if (IfoodEventLedgerEntry.STATUS_PROCESSED.equals(currentStatus)) {
                return RegistrationOutcome.DUPLICATE_PROCESSED;
            }
            if (IfoodEventLedgerEntry.STATUS_RECEIVED.equals(currentStatus)) {
                return RegistrationOutcome.IN_PROGRESS;
            }
            if (IfoodEventLedgerEntry.STATUS_FAILED.equals(currentStatus)) {
                return acquireFailedEvent(existing, receivedEntry);
            }

            log.warn("Unknown iFood event ledger status '{}' for merchant={} event={}. Treating as duplicate.",
                    existing.getStatus(),
                    entry.getMerchantId(),
                    entry.getEventId());
            return RegistrationOutcome.DUPLICATE_PROCESSED;
        }
    }

    public void markProcessed(IfoodEventLedgerEntry entry) {
        if (!isConfigured()) {
            return;
        }
        IfoodEventLedgerEntry existing = findById(entry.getMerchantId(), entry.getEventId());
        IfoodEventLedgerEntry toSave = merge(existing, entry);

        toSave.setStatus(IfoodEventLedgerEntry.STATUS_PROCESSED);
        toSave.setAttempts(defaultAttempts(existing) + 1);
        toSave.setLastError(null);
        toSave.setProcessedAtEpochSeconds(Instant.now().getEpochSecond());
        getTable().putItem(toSave);
    }

    public void markFailed(IfoodEventLedgerEntry entry, String errorMessage) {
        if (!isConfigured()) {
            return;
        }
        IfoodEventLedgerEntry existing = findById(entry.getMerchantId(), entry.getEventId());
        IfoodEventLedgerEntry toSave = merge(existing, entry);

        toSave.setStatus(IfoodEventLedgerEntry.STATUS_FAILED);
        toSave.setAttempts(defaultAttempts(existing) + 1);
        toSave.setLastError(truncate(errorMessage, 800));
        toSave.setProcessedAtEpochSeconds(null);
        getTable().putItem(toSave);
    }

    private RegistrationOutcome acquireFailedEvent(IfoodEventLedgerEntry existing, IfoodEventLedgerEntry receivedEntry) {
        IfoodEventLedgerEntry retryEntry = merge(existing, receivedEntry);
        retryEntry.setStatus(IfoodEventLedgerEntry.STATUS_RECEIVED);
        retryEntry.setLastError(null);

        PutItemEnhancedRequest<IfoodEventLedgerEntry> request = PutItemEnhancedRequest
                .builder(IfoodEventLedgerEntry.class)
                .item(retryEntry)
                .conditionExpression(buildFailedCondition())
                .build();
        try {
            getTable().putItem(request);
            return RegistrationOutcome.ACQUIRED_RETRY;
        } catch (ConditionalCheckFailedException ex) {
            IfoodEventLedgerEntry refreshed = findById(existing.getMerchantId(), existing.getEventId());
            if (refreshed != null && IfoodEventLedgerEntry.STATUS_PROCESSED.equals(normalizeStatus(refreshed.getStatus()))) {
                return RegistrationOutcome.DUPLICATE_PROCESSED;
            }
            return RegistrationOutcome.IN_PROGRESS;
        }
    }

    private IfoodEventLedgerEntry merge(IfoodEventLedgerEntry existing, IfoodEventLedgerEntry incoming) {
        IfoodEventLedgerEntry merged = new IfoodEventLedgerEntry();
        merged.setMerchantId(incoming.getMerchantId());
        merged.setEventId(incoming.getEventId());
        merged.setChannel(firstNonBlank(incoming.getChannel(), existing == null ? null : existing.getChannel()));
        merged.setOrderId(firstNonBlank(incoming.getOrderId(), existing == null ? null : existing.getOrderId()));
        merged.setEventCode(firstNonBlank(incoming.getEventCode(), existing == null ? null : existing.getEventCode()));
        merged.setSource(firstNonBlank(incoming.getSource(), existing == null ? null : existing.getSource()));
        merged.setStatus(existing == null ? incoming.getStatus() : existing.getStatus());
        merged.setAttempts(existing == null ? incoming.getAttempts() : existing.getAttempts());
        merged.setLastError(existing == null ? incoming.getLastError() : existing.getLastError());
        merged.setProcessedAtEpochSeconds(existing == null ? incoming.getProcessedAtEpochSeconds() : existing.getProcessedAtEpochSeconds());

        long now = Instant.now().getEpochSecond();
        Long receivedAt = existing == null ? null : existing.getReceivedAtEpochSeconds();
        merged.setReceivedAtEpochSeconds(receivedAt == null ? now : receivedAt);
        merged.setExpiresAtEpochSeconds(incoming.getExpiresAtEpochSeconds());
        return merged;
    }

    private IfoodEventLedgerEntry toReceivedEntry(IfoodEventLedgerEntry entry) {
        IfoodEventLedgerEntry received = merge(null, entry);
        received.setStatus(IfoodEventLedgerEntry.STATUS_RECEIVED);
        received.setAttempts(0);
        received.setLastError(null);
        received.setProcessedAtEpochSeconds(null);
        return received;
    }

    private IfoodEventLedgerEntry findById(String merchantId, String eventId) {
        if (merchantId == null || merchantId.isBlank() || eventId == null || eventId.isBlank()) {
            return null;
        }
        Key key = Key.builder()
                .partitionValue(merchantId)
                .sortValue(eventId)
                .build();
        return getTable().getItem(key);
    }

    private int defaultAttempts(IfoodEventLedgerEntry entry) {
        if (entry == null || entry.getAttempts() == null || entry.getAttempts() < 0) {
            return 0;
        }
        return entry.getAttempts();
    }

    private Expression buildAbsentCondition() {
        return Expression.builder()
                .expression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                .expressionNames(Map.of(
                        "#pk", "merchantId",
                        "#sk", "eventId"
                ))
                .build();
    }

    private Expression buildFailedCondition() {
        return Expression.builder()
                .expression("#status = :failed")
                .expressionNames(Map.of("#status", "status"))
                .expressionValues(Map.of(
                        ":failed", AttributeValue.builder().s(IfoodEventLedgerEntry.STATUS_FAILED).build()
                ))
                .build();
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        return status.trim().toUpperCase();
    }

    private static String firstNonBlank(String preferred, String fallback) {
        String first = trimToNull(preferred);
        return first != null ? first : trimToNull(fallback);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
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

    public enum RegistrationOutcome {
        ACQUIRED,
        ACQUIRED_RETRY,
        DUPLICATE_PROCESSED,
        IN_PROGRESS;

        public boolean shouldProcess() {
            return this == ACQUIRED || this == ACQUIRED_RETRY;
        }

        public boolean shouldCountAsDuplicate() {
            return this == DUPLICATE_PROCESSED || this == IN_PROGRESS;
        }
    }
}
