// Item.java
package com.oryzem.backend.domain.aws.item;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;

import java.time.LocalDateTime;

@Data // Getters, Setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // Para criar objetos de forma fluente
@DynamoDBTable(tableName = "VW216PA2-Project")
public class Item {

    @DynamoDBHashKey(attributeName = "PartNumberID")
    private String partNumberID;

    @DynamoDBRangeKey(attributeName = "SupplierID")
    private String supplierID;

    @DynamoDBAttribute(attributeName = "CreatedAt")
    @Builder.Default
    private String createdAt = LocalDateTime.now().toString();
}