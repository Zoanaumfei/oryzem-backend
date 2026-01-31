package com.oryzem.backend.modules.items.domain;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class Item {

    private String partNumberVersion;
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

    @DynamoDbSortKey
    @DynamoDbSecondarySortKey(indexNames = "FindByStatus")
    @DynamoDbAttribute("PartNumber#Version")
    public String getPartNumberVersion() {
        return partNumberVersion;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("SupplierID")
    public String getSupplierID() {
        return supplierID;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "FindByStatus")
    @DynamoDbAttribute("Status")
    public ItemStatus getStatus() {
        return status;
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

    // Setters
    public void setPartNumberVersion(String partNumberVersion) {
        this.partNumberVersion = partNumberVersion;
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

