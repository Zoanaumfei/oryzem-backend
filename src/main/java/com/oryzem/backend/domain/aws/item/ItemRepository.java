package com.oryzem.backend.domain.aws.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    // Nome da sua tabela DynamoDB
    private static final String TABLE_NAME = "VW216PA2-Project";

    private DynamoDbTable<Item> getItemTable() {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Item.class));
    }

    public Item save(Item item) {
        log.info("Salvando item: {}/{}", item.getPartNumberID(), item.getSupplierID());
        getItemTable().putItem(item);
        return item;
    }

    public Optional<Item> findById(String partNumberID, String supplierID) {
        log.info("Buscando item: {}/{}", partNumberID, supplierID);

        Key key = Key.builder()
                .partitionValue(partNumberID)
                .sortValue(supplierID)
                .build();

        Item item = getItemTable().getItem(key);
        return Optional.ofNullable(item);
    }

    public boolean exists(String partNumberID, String supplierID) {
        return findById(partNumberID, supplierID).isPresent();
    }

    public void delete(String partNumberID, String supplierID) {
        log.info("Deletando item: {}/{}", partNumberID, supplierID);

        Key key = Key.builder()
                .partitionValue(partNumberID)
                .sortValue(supplierID)
                .build();

        getItemTable().deleteItem(key);
    }
}