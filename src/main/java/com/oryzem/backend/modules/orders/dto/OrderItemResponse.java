package com.oryzem.backend.modules.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Order item returned by the API")
public class OrderItemResponse {

    @Schema(description = "Product identifier", example = "SKU-001")
    private String productId;

    @Schema(description = "Product name snapshot captured at order time", example = "Combo Burger")
    private String nameSnapshot;

    @Schema(description = "Quantity ordered", example = "2")
    private int quantity;

    @Schema(description = "Unit price at order time", example = "29.90")
    private BigDecimal unitPrice;
}
