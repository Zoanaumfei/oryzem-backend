package com.oryzem.backend.modules.items.dto;

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
@Schema(description = "Payload used to create a supplier item")
public class ItemRequest {
    @Schema(description = "Supplier part number", example = "PN123")
    @NotBlank(message = "PartNumber is required")
    @Size(max = 50, message = "PartNumber must be at most 50 characters")
    private String partNumber;

    @Schema(description = "Supplier identifier", example = "SUP456")
    @NotBlank(message = "SupplierID is required")
    @Size(max = 50, message = "SupplierID must be at most 50 characters")
    private String supplierID;

    @Schema(description = "Internal process identifier", example = "PROC-2026-001")
    @NotBlank(message = "ProcessNumber is required")
    @Size(max = 100, message = "ProcessNumber must be at most 100 characters")
    private String processNumber;

    @Schema(description = "Item description", example = "Acabamento lateral direito")
    @NotBlank(message = "PartDescription is required")
    @Size(max = 500, message = "PartDescription must be at most 500 characters")
    private String partDescription;

    @Schema(description = "TBT VFF date in YYYY/MM/DD format", example = "2026/07/15")
    @NotBlank(message = "TbtVffDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "TbtVffDate must be YYYY/MM/DD"
    )
    private String tbtVffDate;

    @Schema(description = "TBT PVS date in YYYY/MM/DD format", example = "2026/05/15")
    @NotBlank(message = "TbtPvsDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "TbtPvsDate must be YYYY/MM/DD"
    )
    private String tbtPvsDate;

    @Schema(description = "TBT 0S date in YYYY/MM/DD format", example = "2026/06/15")
    @NotBlank(message = "Tbt0sDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "Tbt0sDate must be YYYY/MM/DD"
    )
    private String tbt0sDate;

    @Schema(description = "SOP date in YYYY/MM/DD format", example = "2026/09/01")
    @NotBlank(message = "SopDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "SopDate must be YYYY/MM/DD"
    )
    private String sopDate;
}
