package com.oryzem.backend.modules.integrations.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceOrderItemPayload {

    private String sku;
    private String productId;
    private String name;
    private int quantity;
    private BigDecimal unitPrice;
}
