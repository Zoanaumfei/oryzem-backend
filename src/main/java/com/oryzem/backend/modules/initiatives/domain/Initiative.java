package com.oryzem.backend.modules.initiatives.domain;

import com.oryzem.backend.core.tenant.TenantKeyCodec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class Initiative {

    private String pk;
    private String pkKey;
    private String sk;
    private String skKey;
    private String initiativeId;
    private String initiativeName;
    private String initiativeNameLower;
    private String initiativeCode;
    private String initiativeDescription;
    private String initiativeType;
    private String initiativeDueDate;
    private String initiativeStatus;
    private String leaderName;
    private String updatedAt;
    private String createdAt;
    private String tenantId;

    public String getPk() {
        return pk;
    }

    public String getSk() {
        return sk;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPkKey() {
        return pkKey;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSkKey() {
        return skKey;
    }

    @DynamoDbAttribute("initiativeId")
    public String getInitiativeId() {
        return initiativeId;
    }

    @DynamoDbAttribute("initiativeName")
    public String getInitiativeName() {
        return initiativeName;
    }

    @DynamoDbAttribute("initiativeNameLower")
    public String getInitiativeNameLower() {
        return initiativeNameLower;
    }

    @DynamoDbAttribute("initiativeCode")
    public String getInitiativeCode() {
        return initiativeCode;
    }

    @DynamoDbAttribute("initiativeDescription")
    public String getInitiativeDescription() {
        return initiativeDescription;
    }

    @DynamoDbAttribute("initiativeType")
    public String getInitiativeType() {
        return initiativeType;
    }

    @DynamoDbAttribute("initiativeDueDate")
    public String getInitiativeDueDate() {
        return initiativeDueDate;
    }

    @DynamoDbAttribute("initiativeStatus")
    public String getInitiativeStatus() {
        return initiativeStatus;
    }

    @DynamoDbAttribute("leaderName")
    public String getLeaderName() {
        return leaderName;
    }

    @DynamoDbAttribute("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("tenantId")
    public String getTenantId() {
        return tenantId;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setPkKey(String pkKey) {
        this.pkKey = pkKey;
        this.pk = TenantKeyCodec.decode(pkKey);
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public void setSkKey(String skKey) {
        this.skKey = skKey;
        this.sk = TenantKeyCodec.decode(skKey);
    }

    public void setInitiativeId(String initiativeId) {
        this.initiativeId = initiativeId;
    }

    public void setInitiativeName(String initiativeName) {
        this.initiativeName = initiativeName;
    }

    public void setInitiativeNameLower(String initiativeNameLower) {
        this.initiativeNameLower = initiativeNameLower;
    }

    public void setInitiativeCode(String initiativeCode) {
        this.initiativeCode = initiativeCode;
    }

    public void setInitiativeDescription(String initiativeDescription) {
        this.initiativeDescription = initiativeDescription;
    }

    public void setInitiativeType(String initiativeType) {
        this.initiativeType = initiativeType;
    }

    public void setInitiativeDueDate(String initiativeDueDate) {
        this.initiativeDueDate = initiativeDueDate;
    }

    public void setInitiativeStatus(String initiativeStatus) {
        this.initiativeStatus = initiativeStatus;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
