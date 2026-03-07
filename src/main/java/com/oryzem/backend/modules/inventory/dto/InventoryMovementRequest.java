package com.oryzem.backend.modules.inventory.dto;

import com.oryzem.backend.modules.inventory.domain.InventoryMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload used to register an inventory movement")
public class InventoryMovementRequest {

    @Schema(description = "Product identifier", example = "SKU-001")
    @NotBlank(message = "productId is required")
    private String productId;

    @Schema(description = "Inventory movement type", example = "INBOUND")
    @NotNull(message = "type is required")
    private InventoryMovementType type;

    @Schema(description = "Number of units moved", example = "10")
    @Min(value = 1, message = "quantity must be >= 1")
    private int quantity;

    @Schema(description = "Reason for the movement", example = "Manual stock adjustment")
    @NotBlank(message = "reason is required")
    private String reason;

    @Schema(description = "Related order identifier when applicable", example = "ORD-20260306-001")
    private String referenceOrderId;

    @Schema(description = "Minimum stock level after the movement", example = "5")
    @Builder.Default
    private Integer minimumLevel = 0;
}
