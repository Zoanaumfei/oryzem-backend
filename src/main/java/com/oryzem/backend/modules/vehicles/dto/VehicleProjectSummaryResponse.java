package com.oryzem.backend.modules.vehicles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VehicleProjectSummaryResponse(
        @JsonProperty("projectId") String projectId,
        @JsonProperty("projectName") String projectName,
        @JsonProperty("customer") String customer,
        @JsonProperty("als") String als,
        @JsonProperty("SOP") String sop,
        @JsonProperty("status") String status,
        @JsonProperty("rgTemplate") String rgTemplate,
        @JsonProperty("progress") Integer progress,
        @JsonProperty("updatedAt") String updatedAt
) {
}
