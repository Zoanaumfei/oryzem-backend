package com.oryzem.backend.modules.orders.dto;

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
public class OrderItemRequest {

    @NotBlank(message = "productId is required")
    private String productId;

    private String nameSnapshot;

    @Min(value = 1, message = "quantity must be >= 1")
    private int quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice must be >= 0")
    private BigDecimal unitPrice;
}
