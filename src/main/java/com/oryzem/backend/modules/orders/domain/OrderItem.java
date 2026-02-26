package com.oryzem.backend.modules.orders.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private String productId;
    private String nameSnapshot;
    private int quantity;
    private BigDecimal unitPrice;
}
