package com.oryzem.backend.services.dynamodb;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Service
public class DynamoDBService {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public DynamoDBService(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
    }

    public <T> DynamoDbTable<T> getTable(Class<T> clazz, String tableName) {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(clazz));
    }

    // Métodos comuns de DynamoDB
}