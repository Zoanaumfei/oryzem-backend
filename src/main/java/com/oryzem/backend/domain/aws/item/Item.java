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
    private String processNumber;
    private String partDescription;
    private String tbtVffDate;
    private String tbtPvsDate;
    private String tbt0sDate;
    private String sopDate;

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

    @DynamoDbAttribute("ProcessNumber")
    public String getProcessNumber() {
        return processNumber;
    }

    @DynamoDbAttribute("PartDescription")
    public String getPartDescription() {
        return partDescription;
    }

    @DynamoDbAttribute("TbtVffDate")
    public String getTbtVffDate() {
        return tbtVffDate;
    }

    @DynamoDbAttribute("TbtPvsDate")
    public String getTbtPvsDate() {
        return tbtPvsDate;
    }

    @DynamoDbAttribute("Tbt0sDate")
    public String getTbt0sDate() {
        return tbt0sDate;
    }

    @DynamoDbAttribute("SopDate")
    public String getSopDate() {
        return sopDate;
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

    public void setProcessNumber(String processNumber) {
        this.processNumber = processNumber;
    }

    public void setPartDescription(String partDescription) {
        this.partDescription = partDescription;
    }

    public void setTbtVffDate(String tbtVffDate) {
        this.tbtVffDate = tbtVffDate;
    }

    public void setTbtPvsDate(String tbtPvsDate) {
        this.tbtPvsDate = tbtPvsDate;
    }

    public void setTbt0sDate(String tbt0sDate) {
        this.tbt0sDate = tbt0sDate;
    }

    public void setSopDate(String sopDate) {
        this.sopDate = sopDate;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }
}
