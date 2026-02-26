package com.oryzem.backend.modules.integrations.service;

import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderItemPayload;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class IfoodMarketplaceClient implements MarketplaceClient {

    @Override
    public List<MarketplaceOrderPayload> fetchNewOrders() {
        MarketplaceOrderPayload stubOrder = MarketplaceOrderPayload.builder()
                .source(OrderSource.IFOOD)
                .externalOrderId("IFOOD-STUB-1001")
                .customerName("Cliente iFood")
                .items(List.of(
                        MarketplaceOrderItemPayload.builder()
                                .sku("PIZZA-CALABRESA")
                                .name("Pizza Calabresa")
                                .quantity(1)
                                .unitPrice(new BigDecimal("59.90"))
                                .build()
                ))
                .build();
        return List.of(stubOrder);
    }

    @Override
    public void ackOrder(String externalOrderId) {
        log.info("iFood ack order {}", externalOrderId);
        // TODO Integrate with iFood API acknowledgement endpoint.
    }

    @Override
    public void updateOrderStatus(String externalOrderId, OrderStatus status) {
        log.info("iFood update order {} to {}", externalOrderId, status);
        // TODO Integrate with iFood API order status endpoint.
    }
}
