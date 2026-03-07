package com.oryzem.backend.modules.inventory.dto;

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
@Schema(description = "Current inventory position for a product")
public class InventoryItemResponse {

    @Schema(description = "Product identifier", example = "SKU-001")
    private String productId;

    @Schema(description = "Available stock quantity", example = "42")
    private int quantityAvailable;

    @Schema(description = "Configured minimum stock level", example = "10")
    private int minimumLevel;

    @Schema(description = "Last update timestamp", example = "2026-03-06T22:30:00Z")
    private Instant updatedAt;
}
