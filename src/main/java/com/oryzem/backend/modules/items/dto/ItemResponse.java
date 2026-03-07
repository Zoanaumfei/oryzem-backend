package com.oryzem.backend.modules.items.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Supplier item returned by the API")
public class ItemResponse {
    @Schema(description = "Part number with version suffix", example = "PN123#ver00000")
    private String partNumberVersion;
    @Schema(description = "Supplier identifier", example = "SUP456")
    private String supplierID;
    @Schema(description = "Internal process identifier", example = "PROC-2026-001")
    private String processNumber;
    @Schema(description = "Item description", example = "Acabamento lateral direito")
    private String partDescription;
    @Schema(description = "TBT VFF date", example = "2026/07/15")
    private String tbtVffDate;
    @Schema(description = "TBT PVS date", example = "2026/05/15")
    private String tbtPvsDate;
    @Schema(description = "TBT 0S date", example = "2026/06/15")
    private String tbt0sDate;
    @Schema(description = "SOP date", example = "2026/09/01")
    private String sopDate;
    @Schema(description = "Creation timestamp", example = "2025-12-16T20:30:00Z")
    private String createdAt;
    @Schema(description = "Last update timestamp", example = "2026-03-06T22:30:00Z")
    private String updatedAt;
    @Schema(description = "Current item status", example = "ACTIVE")
    private String status;
    @Schema(description = "Additional outcome message", example = "Item criado com sucesso")
    private String message;
}
