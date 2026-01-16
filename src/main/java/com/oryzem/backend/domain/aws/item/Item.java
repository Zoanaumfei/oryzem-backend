// Entidade
// Função: Representa a estrutura de dados da sua tabela DynamoDB

// É o ESPELHO da sua tabela DynamoDB:
// Tabela: VW216PA2-Project
// - PartNumberID (Partition Key)
// - SupplierID (Sort Key)
// - CreatedAt (Atributo adicional)

// Responsabilidades:
// 1. Mapear colunas da tabela para atributos Java
// 2. Definir anotações do DynamoDB (chaves, tipos)
// 3. Ser a "fonte da verdade" da estrutura de dados
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
    private Instant updatedAt;
    private ItemStatus status;

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

    @DynamoDbAttribute("UpdatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @DynamoDbAttribute("Status")
    public ItemStatus getStatus() {
        return status;
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

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }
}
