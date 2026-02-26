package com.oryzem.backend.modules.integrations.service;

import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;

import java.util.List;

public interface MarketplaceClient {

    default OrderSource supportedSource() {
        return null;
    }

    List<MarketplaceOrderPayload> fetchNewOrders();

    void ackOrder(String merchantId, String externalOrderId);

    void updateOrderStatus(String merchantId, String externalOrderId, OrderStatus status);
}
