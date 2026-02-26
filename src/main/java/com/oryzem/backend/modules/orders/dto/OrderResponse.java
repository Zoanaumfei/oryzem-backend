package com.oryzem.backend.modules.orders.dto;

import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private String id;
    private OrderSource source;
    private String externalId;
    private String customerName;

    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();

    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean stockAllocated;
    private String allocationError;
    private String message;
}
