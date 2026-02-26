package com.oryzem.backend.modules.projects.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProjectListResponse(
        String projectId,
        String projectName,
        String customer,
        String als,
        @JsonProperty("SOP") String sop,
        String status,
        String rgTemplate,
        Integer progress,
        String updatedAt
) {
}
