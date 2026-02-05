package com.oryzem.backend.modules.projects.repository;

import com.oryzem.backend.modules.projects.domain.DateIndexItem;
import com.oryzem.backend.modules.projects.domain.MetaItem;
import com.oryzem.backend.modules.projects.domain.MilestoneItem;
import com.oryzem.backend.modules.projects.domain.ProjectKeys;
import com.oryzem.backend.modules.projects.domain.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.IgnoreNullsMode;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProjectRepository {

    private static final int BATCH_SIZE = 25;
    private static final int MAX_RETRIES = 6;
    private static final long BASE_BACKOFF_MS = 100L;

    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.projects-table:OryzemProjects}")
    private String tableName;

    private DynamoDbTable<MetaItem> metaTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(MetaItem.class));
    }

    private DynamoDbTable<MilestoneItem> milestoneTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(MilestoneItem.class));
    }

    private DynamoDbTable<DateIndexItem> dateTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(DateIndexItem.class));
    }

    public Optional<MetaItem> getMeta(String projectId, boolean consistentRead) {
        Key key = Key.builder()
                .partitionValue(ProjectKeys.projectPk(projectId))
                .sortValue(ProjectKeys.metaSk())
                .build();

        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                .key(key)
                .consistentRead(consistentRead)
                .build();

        MetaItem item = metaTable().getItem(request);
        return Optional.ofNullable(item);
    }

    public void createMetaConditionally(MetaItem meta) {
        Expression condition = Expression.builder()
                .expression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                .expressionNames(Map.of("#pk", "PK", "#sk", "SK"))
                .build();

        var request = software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
                .builder(MetaItem.class)
                .item(meta)
                .conditionExpression(condition)
                .build();

        metaTable().putItem(request);
    }

    public void updateMetaStatus(String projectId,
                                 ProjectStatus status,
                                 String requestId,
                                 Instant updatedAt,
                                 ProjectStatus expectedStatus) {

        MetaItem item = MetaItem.builder()
                .pk(ProjectKeys.projectPk(projectId))
                .sk(ProjectKeys.metaSk())
                .status(status)
                .lastRequestId(requestId)
                .updatedAt(updatedAt)
                .entityType(com.oryzem.backend.modules.projects.domain.ProjectEntityType.META)
                .build();

        UpdateItemEnhancedRequest.Builder<MetaItem> builder = UpdateItemEnhancedRequest
                .builder(MetaItem.class)
                .item(item)
                .ignoreNullsMode(IgnoreNullsMode.SCALAR_ONLY);

        if (expectedStatus != null) {
            Expression condition = Expression.builder()
                    .expression("#status = :expected")
                    .expressionNames(Map.of("#status", "status"))
                    .expressionValues(Map.of(
                            ":expected",
                            AttributeValue.builder().s(expectedStatus.name()).build()
                    ))
                    .build();
            builder.conditionExpression(condition);
        }

        metaTable().updateItem(builder.build());
    }

    public List<MilestoneItem> queryProjectMilestones(String projectId) {
        QueryConditional conditional = QueryConditional.sortBeginsWith(
                Key.builder()
                        .partitionValue(ProjectKeys.projectPk(projectId))
                        .sortValue("MS#")
                        .build()
        );

        List<MilestoneItem> items = new ArrayList<>();
        for (var page : milestoneTable().query(r -> r.queryConditional(conditional))) {
            items.addAll(page.items());
        }
        return items;
    }

    public PagedResult<DateIndexItem> queryDateItems(String date, String pageToken, int limit) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(ProjectKeys.datePk(date))
                        .build()
        );

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(conditional)
                .limit(limit);

        if (pageToken != null && !pageToken.isBlank()) {
            requestBuilder.exclusiveStartKey(Map.of(
                    "PK", AttributeValue.builder().s(ProjectKeys.datePk(date)).build(),
                    "SK", AttributeValue.builder().s(pageToken).build()
            ));
        }

        var pages = dateTable().query(requestBuilder.build()).iterator();
        if (!pages.hasNext()) {
            return new PagedResult<>(List.of(), null);
        }

        var page = pages.next();
        String nextToken = null;
        Map<String, AttributeValue> lastKey = page.lastEvaluatedKey();
        if (lastKey != null && !lastKey.isEmpty()) {
            AttributeValue sk = lastKey.get("SK");
            if (sk != null && sk.s() != null && !sk.s().isBlank()) {
                nextToken = sk.s();
            }
        }

        return new PagedResult<>(page.items(), nextToken);
    }

    public void batchPutMilestones(List<MilestoneItem> items) {
        batchPut(milestoneTable(), items);
    }

    public void batchDeleteMilestones(List<MilestoneItem> items) {
        batchDelete(milestoneTable(), items);
    }

    public void batchPutDateItems(List<DateIndexItem> items) {
        batchPut(dateTable(), items);
    }

    public void batchDeleteDateItems(List<DateIndexItem> items) {
        batchDelete(dateTable(), items);
    }

    private <T> void batchPut(DynamoDbTable<T> table, List<T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            List<T> batch = new ArrayList<>(items.subList(i, Math.min(i + BATCH_SIZE, items.size())));
            executePutBatch(table, batch);
        }
    }

    private <T> void batchDelete(DynamoDbTable<T> table, List<T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            List<T> batch = new ArrayList<>(items.subList(i, Math.min(i + BATCH_SIZE, items.size())));
            executeDeleteBatch(table, batch);
        }
    }

    private <T> void executePutBatch(DynamoDbTable<T> table, List<T> batch) {
        List<T> remaining = new ArrayList<>(batch);
        int attempt = 0;

        while (!remaining.isEmpty()) {
            WriteBatch.Builder<T> writeBatch = WriteBatch.builder(table.tableSchema().itemType().rawClass())
                    .mappedTableResource(table);
            for (T item : remaining) {
                writeBatch.addPutItem(item);
            }

            BatchWriteItemEnhancedRequest request = BatchWriteItemEnhancedRequest.builder()
                    .writeBatches(writeBatch.build())
                    .build();

            BatchWriteResult result = enhancedClient.batchWriteItem(request);
            List<T> unprocessed = result.unprocessedPutItemsForTable(table);
            remaining = unprocessed == null ? Collections.emptyList() : unprocessed;

            if (!remaining.isEmpty()) {
                backoff(++attempt);
                if (attempt > MAX_RETRIES) {
                    throw new IllegalStateException("DynamoDB batch put exceeded retry limit");
                }
            }
        }
    }

    private <T> void executeDeleteBatch(DynamoDbTable<T> table, List<T> batch) {
        List<Key> remaining = buildKeys(table, batch);
        int attempt = 0;

        while (!remaining.isEmpty()) {
            WriteBatch.Builder<T> writeBatch = WriteBatch.builder(table.tableSchema().itemType().rawClass())
                    .mappedTableResource(table);
            for (Key key : remaining) {
                writeBatch.addDeleteItem(key);
            }

            BatchWriteItemEnhancedRequest request = BatchWriteItemEnhancedRequest.builder()
                    .writeBatches(writeBatch.build())
                    .build();

            BatchWriteResult result = enhancedClient.batchWriteItem(request);
            List<Key> unprocessed = result.unprocessedDeleteItemsForTable(table);
            remaining = unprocessed == null ? Collections.emptyList() : unprocessed;

            if (!remaining.isEmpty()) {
                backoff(++attempt);
                if (attempt > MAX_RETRIES) {
                    throw new IllegalStateException("DynamoDB batch delete exceeded retry limit");
                }
            }
        }
    }

    private <T> List<Key> buildKeys(DynamoDbTable<T> table, List<T> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        TableSchema<T> schema = table.tableSchema();
        TableMetadata metadata = schema.tableMetadata();
        List<String> partitionKeys = metadata.indexPartitionKeys(TableMetadata.primaryIndexName());
        List<String> sortKeys = metadata.indexSortKeys(TableMetadata.primaryIndexName());

        List<Key> keys = new ArrayList<>(items.size());
        for (T item : items) {
            if (item == null) {
                continue;
            }

            Map<String, AttributeValue> attrMap = schema.itemToMap(item, metadata.primaryKeys());
            List<AttributeValue> partitionValues = new ArrayList<>();
            for (String keyName : partitionKeys) {
                AttributeValue value = attrMap.get(keyName);
                if (value != null) {
                    partitionValues.add(value);
                }
            }

            if (partitionValues.isEmpty()) {
                continue;
            }

            Key.Builder builder = Key.builder().partitionValues(partitionValues);
            if (!sortKeys.isEmpty()) {
                List<AttributeValue> sortValues = new ArrayList<>();
                for (String keyName : sortKeys) {
                    AttributeValue value = attrMap.get(keyName);
                    if (value != null) {
                        sortValues.add(value);
                    }
                }
                if (!sortValues.isEmpty()) {
                    builder.sortValues(sortValues);
                }
            }

            keys.add(builder.build());
        }

        return keys;
    }

    private void backoff(int attempt) {
        long delay = BASE_BACKOFF_MS * (1L << Math.min(attempt, 10));
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DynamoDB batch operation interrupted", ex);
        }
    }
}
