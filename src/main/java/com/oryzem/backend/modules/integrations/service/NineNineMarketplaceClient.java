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
public class NineNineMarketplaceClient implements MarketplaceClient {

    @Override
    public OrderSource supportedSource() {
        return OrderSource.NINENINE;
    }

    @Override
    public List<MarketplaceOrderPayload> fetchNewOrders() {
        MarketplaceOrderPayload stubOrder = MarketplaceOrderPayload.builder()
                .source(OrderSource.NINENINE)
                .merchantId("99FOOD-STUB-MERCHANT")
                .externalOrderId("99FOOD-STUB-2001")
                .customerName("Cliente 99Food")
                .items(List.of(
                        MarketplaceOrderItemPayload.builder()
                                .sku("REFRI-COLA-2L")
                                .name("Refrigerante Cola 2L")
                                .quantity(2)
                                .unitPrice(new BigDecimal("12.00"))
                                .build()
                ))
                .build();
        return List.of(stubOrder);
    }

    @Override
    public void ackOrder(String merchantId, String externalOrderId) {
        log.info("99Food ack order {} (merchant={})", externalOrderId, merchantId);
        // TODO Integrate with 99Food API acknowledgement endpoint.
    }

    @Override
    public void updateOrderStatus(String merchantId, String externalOrderId, OrderStatus status) {
        log.info("99Food update order {} to {} (merchant={})", externalOrderId, status, merchantId);
        // TODO Integrate with 99Food API order status endpoint.
    }
}
