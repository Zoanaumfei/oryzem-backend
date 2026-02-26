package com.oryzem.backend.modules.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oryzem.backend.modules.integrations.config.IfoodProperties;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.integrations.dto.IfoodIngestionResponse;
import com.oryzem.backend.modules.integrations.repository.IfoodEventLedgerRepository;
import com.oryzem.backend.modules.integrations.service.IfoodIngestionService;
import com.oryzem.backend.modules.integrations.service.IfoodMarketplaceClient;
import com.oryzem.backend.modules.integrations.service.MarketplaceOrderMapper;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IfoodIngestionServiceTest {

    @Mock
    private IfoodMarketplaceClient ifoodClient;

    @Mock
    private MarketplaceOrderMapper marketplaceOrderMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private IfoodEventLedgerRepository eventLedgerRepository;

    @Test
    void shouldPersistWebhookEventAndImportOrderWhenLedgerEnabled() {
        IfoodIngestionService service = buildService(true, true);

        MarketplaceOrderPayload payload = MarketplaceOrderPayload.builder()
                .source(OrderSource.IFOOD)
                .merchantId("merchant-1")
                .externalOrderId("order-1")
                .customerName("Cliente")
                .build();
        CreateOrderRequest request = CreateOrderRequest.builder().build();
        OrderResponse created = OrderResponse.builder().message("Order created successfully").build();

        when(eventLedgerRepository.saveIfAbsent(any())).thenReturn(true);
        when(ifoodClient.fetchOrderById("merchant-1", "order-1")).thenReturn(payload);
        when(marketplaceOrderMapper.toCreateOrderRequest(payload)).thenReturn(request);
        when(orderService.createOrder(request)).thenReturn(created);

        IfoodIngestionResponse response = service.ingestFromWebhook(
                "[{\"id\":\"evt-1\",\"code\":\"PLC\",\"orderId\":\"order-1\",\"merchantId\":\"merchant-1\"}]",
                null
        );

        assertThat(response.getProcessedEvents()).isEqualTo(1);
        assertThat(response.getImportedOrders()).isEqualTo(1);
        assertThat(response.getDuplicateOrders()).isEqualTo(0);
        assertThat(response.getFailedEvents()).isEqualTo(0);

        verify(eventLedgerRepository, times(1)).saveIfAbsent(any());
        verify(ifoodClient, times(1)).fetchOrderById("merchant-1", "order-1");
    }

    @Test
    void shouldSkipDuplicateWebhookEventAlreadyInLedger() {
        IfoodIngestionService service = buildService(true, true);
        when(eventLedgerRepository.saveIfAbsent(any())).thenReturn(false);

        IfoodIngestionResponse response = service.ingestFromWebhook(
                "[{\"id\":\"evt-2\",\"code\":\"PLC\",\"orderId\":\"order-2\",\"merchantId\":\"merchant-2\"}]",
                null
        );

        assertThat(response.getProcessedEvents()).isEqualTo(1);
        assertThat(response.getImportedOrders()).isEqualTo(0);
        assertThat(response.getDuplicateOrders()).isEqualTo(1);
        assertThat(response.getFailedEvents()).isEqualTo(0);

        verify(ifoodClient, never()).fetchOrderById(any(), any());
        verify(orderService, never()).createOrder(any());
    }

    @Test
    void shouldFallbackToInMemoryDedupeWhenLedgerDisabled() {
        IfoodIngestionService service = buildService(false, false);

        MarketplaceOrderPayload payload = MarketplaceOrderPayload.builder()
                .source(OrderSource.IFOOD)
                .merchantId("merchant-3")
                .externalOrderId("order-3")
                .customerName("Cliente")
                .build();
        CreateOrderRequest request = CreateOrderRequest.builder().build();
        OrderResponse created = OrderResponse.builder().message("Order created successfully").build();

        when(ifoodClient.fetchOrderById("merchant-3", "order-3")).thenReturn(payload);
        when(marketplaceOrderMapper.toCreateOrderRequest(payload)).thenReturn(request);
        when(orderService.createOrder(request)).thenReturn(created);

        String payloadJson = "[{\"id\":\"evt-3\",\"code\":\"PLC\",\"orderId\":\"order-3\",\"merchantId\":\"merchant-3\"}]";

        IfoodIngestionResponse first = service.ingestFromWebhook(payloadJson, null);
        IfoodIngestionResponse second = service.ingestFromWebhook(payloadJson, null);

        assertThat(first.getImportedOrders()).isEqualTo(1);
        assertThat(second.getImportedOrders()).isEqualTo(0);
        assertThat(second.getDuplicateOrders()).isEqualTo(1);

        verify(eventLedgerRepository, never()).saveIfAbsent(any());
        verify(orderService, times(1)).createOrder(ArgumentMatchers.eq(request));
    }

    private IfoodIngestionService buildService(boolean eventLedgerEnabled, boolean ledgerConfigured) {
        IfoodProperties properties = new IfoodProperties(
                true,
                "https://merchant-api.ifood.com.br",
                "client-id",
                "client-secret",
                true,
                null,
                eventLedgerEnabled,
                7,
                false,
                300,
                List.of("merchant-1"),
                List.of("PLC"),
                null,
                "Cancelamento solicitado pela loja",
                20,
                60
        );
        if (eventLedgerEnabled) {
            when(eventLedgerRepository.isConfigured()).thenReturn(ledgerConfigured);
        }
        return new IfoodIngestionService(
                ifoodClient,
                marketplaceOrderMapper,
                orderService,
                properties,
                eventLedgerRepository,
                new ObjectMapper()
        );
    }
}
