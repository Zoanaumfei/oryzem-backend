package com.oryzem.backend.modules.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private String productId;
    private String nameSnapshot;
    private int quantity;
    private BigDecimal unitPrice;
}
