package com.oryzem.backend.modules.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Single order item sent when creating an order")
public class OrderItemRequest {

    @Schema(description = "Product identifier", example = "SKU-001")
    @NotBlank(message = "productId is required")
    private String productId;

    @Schema(description = "Product name snapshot captured at order time", example = "Combo Burger")
    private String nameSnapshot;

    @Schema(description = "Quantity ordered", example = "2")
    @Min(value = 1, message = "quantity must be >= 1")
    private int quantity;

    @Schema(description = "Unit price at order time", example = "29.90")
    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice must be >= 0")
    private BigDecimal unitPrice;
}
