package com.oryzem.backend.modules.inventory.dto;

import com.oryzem.backend.modules.inventory.domain.InventoryMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Inventory movement returned after registration")
public class InventoryMovementResponse {

    @Schema(description = "Inventory movement identifier", example = "mov-001")
    private String id;

    @Schema(description = "Product identifier", example = "SKU-001")
    private String productId;

    @Schema(description = "Inventory movement type", example = "INBOUND")
    private InventoryMovementType type;

    @Schema(description = "Number of units moved", example = "10")
    private int quantity;

    @Schema(description = "Reason for the movement", example = "Manual stock adjustment")
    private String reason;

    @Schema(description = "Related order identifier when applicable", example = "ORD-20260306-001")
    private String referenceOrderId;

    @Schema(description = "Creation timestamp", example = "2026-03-06T22:15:30Z")
    private Instant createdAt;
}
