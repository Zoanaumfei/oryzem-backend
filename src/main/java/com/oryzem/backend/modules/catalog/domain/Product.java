package com.oryzem.backend.modules.catalog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private String id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal unitPrice;

    @Builder.Default
    private boolean active = true;

    private Instant createdAt;
    private Instant updatedAt;
}
