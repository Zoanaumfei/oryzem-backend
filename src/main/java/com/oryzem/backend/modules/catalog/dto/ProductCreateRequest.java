package com.oryzem.backend.modules.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Payload used to create a catalog product")
public class ProductCreateRequest {

    @Schema(description = "Stock keeping unit", example = "SKU-001")
    @NotBlank(message = "sku is required")
    @Size(max = 64, message = "sku must have at most 64 characters")
    private String sku;

    @Schema(description = "Display name of the product", example = "Combo Burger")
    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must have at most 120 characters")
    private String name;

    @Schema(description = "Product category", example = "SNACKS")
    @NotBlank(message = "category is required")
    @Size(max = 80, message = "category must have at most 80 characters")
    private String category;

    @Schema(description = "Unit price charged for the product", example = "29.90")
    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice must be >= 0")
    private BigDecimal unitPrice;

    @Schema(description = "Whether the product is active for use", example = "true")
    @Builder.Default
    private Boolean active = Boolean.TRUE;
}
