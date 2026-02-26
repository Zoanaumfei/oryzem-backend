package com.oryzem.backend.modules.inventory.dto;

import com.oryzem.backend.modules.inventory.domain.InventoryMovementType;
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
public class InventoryMovementRequest {

    @NotBlank(message = "productId is required")
    private String productId;

    @NotNull(message = "type is required")
    private InventoryMovementType type;

    @Min(value = 1, message = "quantity must be >= 1")
    private int quantity;

    @NotBlank(message = "reason is required")
    private String reason;

    private String referenceOrderId;

    @Builder.Default
    private Integer minimumLevel = 0;
}
