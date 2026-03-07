package com.oryzem.backend.modules.orders.dto;

import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Order returned by the API")
public class OrderResponse {

    @Schema(description = "Order identifier", example = "ORD-20260306-001")
    private String id;

    @Schema(description = "Origin of the order", example = "INTERNAL")
    private OrderSource source;

    @Schema(description = "Merchant or integration account identifier", example = "ifood-store-001")
    private String merchantId;

    @Schema(description = "External marketplace order identifier", example = "ABC123")
    private String externalId;

    @Schema(description = "Customer display name", example = "Ana Silva")
    private String customerName;

    @Schema(description = "Items included in the order")
    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();

    @Schema(description = "Total order amount", example = "59.80")
    private BigDecimal totalAmount;

    @Schema(description = "Current order status", example = "CONFIRMED")
    private OrderStatus status;

    @Schema(description = "Creation timestamp", example = "2026-03-06T22:15:30Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-03-06T22:20:30Z")
    private Instant updatedAt;

    @Schema(description = "Store identifier", example = "store-001")
    private String storeId;

    @Schema(description = "Integration account identifier", example = "ifood-account-001")
    private String integrationAccountId;

    @Schema(description = "Whether stock allocation was completed", example = "true")
    private boolean stockAllocated;

    @Schema(description = "Allocation error when stock reservation fails", example = "Insufficient inventory for SKU-001")
    private String allocationError;

    @Schema(description = "Timeline of important order events")
    @Builder.Default
    private List<OrderTimelineEventResponse> timeline = new ArrayList<>();

    @Schema(description = "Additional outcome message", example = "Order created successfully")
    private String message;
}
