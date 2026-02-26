package com.oryzem.backend.modules.orders.dto;

import com.oryzem.backend.modules.orders.domain.OrderSource;
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
public class CreateOrderRequest {

    @NotNull(message = "source is required")
    private OrderSource source;

    private String merchantId;

    private String externalId;

    @NotBlank(message = "customerName is required")
    private String customerName;

    @Valid
    @NotEmpty(message = "items is required")
    @Builder.Default
    private List<OrderItemRequest> items = new ArrayList<>();
}
