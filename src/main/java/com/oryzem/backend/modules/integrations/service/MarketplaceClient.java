package com.oryzem.backend.modules.integrations.service;

import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.orders.domain.OrderStatus;

import java.util.List;

public interface MarketplaceClient {

    List<MarketplaceOrderPayload> fetchNewOrders();

    void ackOrder(String externalOrderId);

    void updateOrderStatus(String externalOrderId, OrderStatus status);
}
