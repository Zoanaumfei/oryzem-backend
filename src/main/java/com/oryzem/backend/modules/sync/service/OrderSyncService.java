package com.oryzem.backend.modules.sync.service;

import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.integrations.service.MarketplaceClient;
import com.oryzem.backend.modules.integrations.service.MarketplaceOrderMapper;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.service.OrderService;
import com.oryzem.backend.modules.sync.dto.OrderSyncResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSyncService {

    private final List<MarketplaceClient> marketplaceClients;
    private final MarketplaceOrderMapper marketplaceOrderMapper;
    private final OrderService orderService;

    public OrderSyncResponse syncOrders() {
        int importedCount = 0;
        int duplicateCount = 0;
        int failedCount = 0;
        List<String> orderIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MarketplaceClient client : marketplaceClients) {
            List<MarketplaceOrderPayload> payloads = client.fetchNewOrders();
            for (MarketplaceOrderPayload payload : payloads) {
                try {
                    OrderResponse response = orderService.createOrder(
                            marketplaceOrderMapper.toCreateOrderRequest(payload)
                    );

                    if ("Order already exists".equals(response.getMessage())) {
                        duplicateCount++;
                    } else {
                        importedCount++;
                    }
                    orderIds.add(response.getId());
                    client.ackOrder(payload.getExternalOrderId());
                } catch (Exception ex) {
                    failedCount++;
                    String error = "Failed to sync external order "
                            + payload.getExternalOrderId()
                            + ": " + ex.getMessage();
                    errors.add(error);
                    log.warn(error, ex);
                }
            }
        }

        return OrderSyncResponse.builder()
                .importedCount(importedCount)
                .duplicateCount(duplicateCount)
                .failedCount(failedCount)
                .orderIds(orderIds)
                .errors(errors)
                .build();
    }
}
