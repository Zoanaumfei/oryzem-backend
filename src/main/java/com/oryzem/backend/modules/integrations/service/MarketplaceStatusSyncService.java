package com.oryzem.backend.modules.integrations.service;

import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceStatusSyncService {

    private final List<MarketplaceClient> clients;

    public void publish(OrderSource source, String merchantId, String externalOrderId, OrderStatus status) {
        if (source == null
                || source == OrderSource.INTERNAL
                || merchantId == null
                || merchantId.isBlank()
                || externalOrderId == null
                || externalOrderId.isBlank()
                || status == null) {
            return;
        }

        MarketplaceClient client = resolveClientBySource().get(source);
        if (client == null) {
            log.debug("No marketplace client registered for source {}", source);
            return;
        }

        client.updateOrderStatus(merchantId.trim(), externalOrderId.trim(), status);
    }

    private Map<OrderSource, MarketplaceClient> resolveClientBySource() {
        Map<OrderSource, MarketplaceClient> clientBySource = new EnumMap<>(OrderSource.class);
        for (MarketplaceClient client : clients) {
            OrderSource supportedSource = client.supportedSource();
            if (supportedSource == null) {
                continue;
            }

            MarketplaceClient previous = clientBySource.putIfAbsent(supportedSource, client);
            if (previous != null && previous != client) {
                log.warn("Multiple marketplace clients found for source {}. Using {}",
                        supportedSource,
                        previous.getClass().getSimpleName());
            }
        }
        return clientBySource;
    }
}
