package com.oryzem.backend.modules.integrations.domain;

import com.oryzem.backend.modules.orders.domain.OrderSource;
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
public class MarketplaceOrderPayload {

    private OrderSource source;
    private String externalOrderId;
    private String customerName;

    @Builder.Default
    private List<MarketplaceOrderItemPayload> items = new ArrayList<>();
}
