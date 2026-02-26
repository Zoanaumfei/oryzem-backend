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
public class InventoryItem {

    private String productId;
    private int quantityAvailable;
    private int minimumLevel;
    private Instant updatedAt;
}
