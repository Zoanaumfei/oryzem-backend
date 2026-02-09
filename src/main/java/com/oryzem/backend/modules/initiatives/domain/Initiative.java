package com.oryzem.backend.modules.initiatives.domain;

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
    private String sk;
    private String initiativeId;
    private String initiativeName;
    private String initiativeNameLower;
    private String initiativeDescription;
    private String initiativeType;
    private String initiativeDueDate;
    private String initiativeStatus;
    private String leaderName;
    private String updatedAt;
    private String createdAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getSk() {
        return sk;
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

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setSk(String sk) {
        this.sk = sk;
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
}
