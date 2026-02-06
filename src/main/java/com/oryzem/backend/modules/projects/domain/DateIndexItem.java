package com.oryzem.backend.modules.projects.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class DateIndexItem {

    private String pk;
    private String sk;
    private String projectId;
    private String projectName;
    private Integer als;
    private String alsDescription;
    private Gate gate;
    private Phase phase;
    private String date;
    private Instant updatedAt;
    private ProjectEntityType entityType;

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

    @DynamoDbAttribute("projectId")
    public String getProjectId() {
        return projectId;
    }

    @DynamoDbAttribute("projectName")
    public String getProjectName() {
        return projectName;
    }

    @DynamoDbAttribute("als")
    public Integer getAls() {
        return als;
    }

    @DynamoDbAttribute("alsDescription")
    public String getAlsDescription() {
        return alsDescription;
    }

    @DynamoDbAttribute("gate")
    public Gate getGate() {
        return gate;
    }

    @DynamoDbAttribute("phase")
    public Phase getPhase() {
        return phase;
    }

    @DynamoDbAttribute("date")
    public String getDate() {
        return date;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @DynamoDbAttribute("entityType")
    public ProjectEntityType getEntityType() {
        return entityType;
    }
}
