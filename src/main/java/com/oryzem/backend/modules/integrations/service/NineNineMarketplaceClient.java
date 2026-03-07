package com.oryzem.backend.modules.integrations.service;

import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class NineNineMarketplaceClient implements MarketplaceClient {

    private final boolean enabled;
    private final AtomicBoolean warnedLegacy = new AtomicBoolean(false);

    public NineNineMarketplaceClient(
            @Value("${app.integrations.99food.enabled:false}") boolean enabled
    ) {
        this.enabled = enabled;
    }

    @Override
    public OrderSource supportedSource() {
        return OrderSource.NINENINE;
    }

    @Override
    public List<MarketplaceOrderPayload> fetchNewOrders() {
        if (!enabled) {
            return List.of();
        }
        warnLegacyNotImplemented();
        return List.of();
    }

    @Override
    public void ackOrder(String merchantId, String externalOrderId) {
        if (!enabled) {
            return;
        }
        warnLegacyNotImplemented();
    }

    @Override
    public void updateOrderStatus(String merchantId, String externalOrderId, OrderStatus status) {
        if (!enabled) {
            return;
        }
        warnLegacyNotImplemented();
    }

    private void warnLegacyNotImplemented() {
        if (warnedLegacy.compareAndSet(false, true)) {
            log.warn("99Food client is legacy and disabled for real traffic. Use RAPPI integration instead.");
        }
    }
}
