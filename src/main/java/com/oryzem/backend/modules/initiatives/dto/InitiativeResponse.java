package com.oryzem.backend.modules.initiatives.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Initiative returned by the API")
public class InitiativeResponse {

    @Schema(description = "Business identifier of the initiative", example = "INIT-2026-001")
    private String initiativeId;

    @Schema(description = "Display name of the initiative", example = "Revisao do processo de compras")
    private String initiativeName;

    @Schema(description = "Normalized initiative name used for searches", example = "revisao do processo de compras")
    private String initiativeNameLower;

    @Schema(description = "Short initiative code", example = "CPR-001")
    private String initiativeCode;

    @Schema(description = "Detailed description of the initiative", example = "Padronizar o fluxo de compras e aprovacoes.")
    private String initiativeDescription;

    @Schema(description = "Type or category of initiative", example = "PROCESS")
    private String initiativeType;

    @Schema(description = "Due date in YYYY-MM-DD format", example = "2026-06-30")
    private String initiativeDueDate;

    @Schema(description = "Execution status", example = "IN_PROGRESS")
    private String initiativeStatus;

    @Schema(description = "Leader responsible for the initiative", example = "Mariana Costa")
    private String leaderName;

    @Schema(description = "Last update timestamp", example = "2026-03-06T22:30:00Z")
    private String updatedAt;

    @Schema(description = "Creation timestamp", example = "2026-03-06T22:15:30Z")
    private String createdAt;

    @Schema(description = "Additional outcome message", example = "Initiative created successfully")
    private String message;
}
