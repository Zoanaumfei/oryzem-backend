package com.oryzem.backend.modules.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Catalog product returned by the API")
public class ProductResponse {

    @Schema(description = "Internal product identifier", example = "prod-001")
    private String id;

    @Schema(description = "Stock keeping unit", example = "SKU-001")
    private String sku;

    @Schema(description = "Display name of the product", example = "Combo Burger")
    private String name;

    @Schema(description = "Product category", example = "SNACKS")
    private String category;

    @Schema(description = "Unit price charged for the product", example = "29.90")
    private BigDecimal unitPrice;

    @Schema(description = "Whether the product is active for use", example = "true")
    private boolean active;

    @Schema(description = "Creation timestamp", example = "2026-03-06T22:15:30Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-03-06T22:20:30Z")
    private Instant updatedAt;
}
