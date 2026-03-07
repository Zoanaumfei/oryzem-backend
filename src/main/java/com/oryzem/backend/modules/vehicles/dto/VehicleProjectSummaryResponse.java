package com.oryzem.backend.modules.vehicles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Compact ALS summary returned in global project listings")
public record VehicleProjectSummaryResponse(
        @JsonProperty("projectId") @Schema(description = "Project identifier", example = "VW247_1") String projectId,
        @JsonProperty("projectName") @Schema(description = "Project name", example = "TCross PA2") String projectName,
        @JsonProperty("customer") @Schema(description = "Customer name", example = "Volkswagen") String customer,
        @JsonProperty("als") @Schema(description = "ALS identifier", example = "ALS1") String als,
        @JsonProperty("SOP") @Schema(description = "SOP milestone date", example = "2026-09-01") String sop,
        @JsonProperty("status") @Schema(description = "Current project status", example = "IN_PROGRESS") String status,
        @JsonProperty("rgTemplate") @Schema(description = "Reference RG template", example = "RG-ALS-2026") String rgTemplate,
        @JsonProperty("progress") @Schema(description = "Completion percentage", example = "65") Integer progress,
        @JsonProperty("updatedAt") @Schema(description = "Last update timestamp", example = "2026-03-06T22:30:00Z") String updatedAt
) {
}
