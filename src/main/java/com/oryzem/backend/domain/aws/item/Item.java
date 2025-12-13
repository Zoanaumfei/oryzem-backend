// Item.java (versão com Instant)
package com.oryzem.backend.domain.aws.item;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class Item {

    private String partNumberID;
    private String supplierID;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PartNumberID")
    public String getPartNumberID() {
        return partNumberID;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SupplierID")
    public String getSupplierID() {
        return supplierID;
    }

    @DynamoDbAttribute("CreatedAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setPartNumberID(String partNumberID) {
        this.partNumberID = partNumberID;
    }

    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}