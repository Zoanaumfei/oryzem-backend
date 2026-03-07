package com.oryzem.backend.modules.initiatives.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload used to create or update an initiative")
public class InitiativeRequest {

    @Schema(description = "Business identifier of the initiative", example = "INIT-2026-001")
    @NotBlank(message = "InitiativeId is required")
    @Size(max = 100, message = "InitiativeId must be at most 100 characters")
    private String initiativeId;

    @Schema(description = "Display name of the initiative", example = "Revisao do processo de compras")
    @NotBlank(message = "InitiativeName is required")
    @Size(max = 200, message = "InitiativeName must be at most 200 characters")
    private String initiativeName;

    @Schema(description = "Detailed description of the initiative", example = "Padronizar o fluxo de compras e aprovacoes.")
    @NotBlank(message = "InitiativeDescription is required")
    @Size(max = 2000, message = "InitiativeDescription must be at most 2000 characters")
    private String initiativeDescription;

    @Schema(description = "Type or category of initiative", example = "PROCESS")
    @NotBlank(message = "InitiativeType is required")
    @Size(max = 100, message = "InitiativeType must be at most 100 characters")
    private String initiativeType;

    @Schema(description = "Due date in YYYY-MM-DD format", example = "2026-06-30")
    @NotBlank(message = "InitiativeDueDate is required")
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "InitiativeDueDate must be YYYY-MM-DD"
    )
    private String initiativeDueDate;

    @Schema(description = "Execution status", example = "IN_PROGRESS")
    @NotBlank(message = "InitiativeStatus is required")
    @Pattern(
            regexp = "^(IN_PROGRESS|CONCLUDED)$",
            message = "InitiativeStatus must be IN_PROGRESS or CONCLUDED"
    )
    private String initiativeStatus;

    @Schema(description = "Leader responsible for the initiative", example = "Mariana Costa")
    @Size(max = 200, message = "LeaderName must be at most 200 characters")
    private String leaderName;
}
