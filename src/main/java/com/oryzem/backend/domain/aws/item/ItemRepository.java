package com.oryzem.backend.domain.aws.item;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor // Cria construtor com final fields
public class ItemRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public Item save(Item item) {
        dynamoDBMapper.save(item);
        return item;
    }

    public Item findById(String partNumberID, String supplierID) {
        return dynamoDBMapper.load(Item.class, partNumberID, supplierID);
    }

    public boolean exists(String partNumberID, String supplierID) {
        return findById(partNumberID, supplierID) != null;
    }

    public void delete(String partNumberID, String supplierID) {
        Item item = findById(partNumberID, supplierID);
        if (item != null) {
            dynamoDBMapper.delete(item);
        }
    }
}
