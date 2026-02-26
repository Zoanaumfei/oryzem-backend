package com.oryzem.backend.modules.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    private String id;
    private String productId;
    private InventoryMovementType type;
    private int quantity;
    private String reason;
    private String referenceOrderId;
    private Instant createdAt;
}
