package com.oryzem.backend.modules.orders.dto;

import com.oryzem.backend.modules.orders.domain.OrderSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload used to create an internal or marketplace order")
public class CreateOrderRequest {

    @Schema(description = "Origin of the order", example = "INTERNAL")
    @NotNull(message = "source is required")
    private OrderSource source;

    @Schema(description = "Merchant or integration account identifier", example = "ifood-store-001")
    private String merchantId;

    @Schema(description = "External marketplace order identifier", example = "ABC123")
    private String externalId;

    @Schema(description = "Customer display name", example = "Ana Silva")
    @NotBlank(message = "customerName is required")
    private String customerName;

    @Schema(description = "Items included in the order")
    @Valid
    @NotEmpty(message = "items is required")
    @Builder.Default
    private List<OrderItemRequest> items = new ArrayList<>();
}
