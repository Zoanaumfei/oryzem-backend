package com.oryzem.backend.modules.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotBlank(message = "sku is required")
    @Size(max = 64, message = "sku must have at most 64 characters")
    private String sku;

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must have at most 120 characters")
    private String name;

    @NotBlank(message = "category is required")
    @Size(max = 80, message = "category must have at most 80 characters")
    private String category;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice must be >= 0")
    private BigDecimal unitPrice;

    @Builder.Default
    private Boolean active = Boolean.TRUE;
}
