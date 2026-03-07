package com.oryzem.backend.modules.items.domain;

import com.oryzem.backend.core.tenant.TenantKeyCodec;
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
    private String partNumberVersionKey;
    private String supplierID;
    private String supplierKey;
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
    private String statusKey;
    private String tenantId;

    public String getPartNumberVersion() {
        return partNumberVersion;
    }

    public String getSupplierID() {
        return supplierID;
    }

    public ItemStatus getStatus() {
        return status;
    }

    @DynamoDbSortKey
    @DynamoDbSecondarySortKey(indexNames = "FindByStatus")
    @DynamoDbAttribute("PartNumber#Version")
    public String getPartNumberVersionKey() {
        return partNumberVersionKey;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("SupplierID")
    public String getSupplierKey() {
        return supplierKey;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "FindByStatus")
    @DynamoDbAttribute("Status")
    public String getStatusKey() {
        return statusKey;
    }

    @DynamoDbAttribute("tenantId")
    public String getTenantId() {
        return tenantId;
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

    public void setPartNumberVersionKey(String partNumberVersionKey) {
        this.partNumberVersionKey = partNumberVersionKey;
        this.partNumberVersion = TenantKeyCodec.decode(partNumberVersionKey);
    }

    public void setSupplierID(String supplierID) {
        this.supplierID = supplierID;
    }

    public void setSupplierKey(String supplierKey) {
        this.supplierKey = supplierKey;
        this.supplierID = TenantKeyCodec.decode(supplierKey);
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

    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
        String decoded = TenantKeyCodec.decode(statusKey);
        this.status = decoded == null ? null : ItemStatus.valueOf(decoded);
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}

