package com.oryzem.backend.modules.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oryzem.backend.modules.integrations.config.IfoodProperties;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.integrations.dto.IfoodIngestionResponse;
import com.oryzem.backend.modules.integrations.repository.IfoodEventLedgerRepository;
import com.oryzem.backend.modules.integrations.repository.IfoodEventLedgerRepository.RegistrationOutcome;
import com.oryzem.backend.modules.integrations.service.CanonicalOrderIngestBridge;
import com.oryzem.backend.modules.integrations.service.IfoodIngestionService;
import com.oryzem.backend.modules.integrations.service.IfoodMarketplaceClient;
import com.oryzem.backend.modules.integrations.service.MarketplaceOrderMapper;
import com.oryzem.backend.modules.messaging.service.OrderIngestResult;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private IfoodEventLedgerRepository eventLedgerRepository;

    @Mock
    private CanonicalOrderIngestBridge canonicalOrderIngestBridge;

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

        when(eventLedgerRepository.registerForProcessing(any())).thenReturn(RegistrationOutcome.ACQUIRED);
        when(ifoodClient.fetchOrderById("merchant-1", "order-1")).thenReturn(payload);
        when(marketplaceOrderMapper.toCreateOrderRequest(payload)).thenReturn(request);
        when(canonicalOrderIngestBridge.ingestIfoodPayload(any(), any(), any(), any())).thenReturn(OrderIngestResult.CREATED);

        IfoodIngestionResponse response = service.ingestFromWebhook(
                "[{\"id\":\"evt-1\",\"code\":\"PLC\",\"orderId\":\"order-1\",\"merchantId\":\"merchant-1\"}]",
                null
        );

        assertThat(response.getProcessedEvents()).isEqualTo(1);
        assertThat(response.getImportedOrders()).isEqualTo(1);
        assertThat(response.getDuplicateOrders()).isEqualTo(0);
        assertThat(response.getFailedEvents()).isEqualTo(0);

        verify(eventLedgerRepository, times(1)).registerForProcessing(any());
        verify(eventLedgerRepository, times(1)).markProcessed(any());
        verify(ifoodClient, times(1)).fetchOrderById("merchant-1", "order-1");
    }

    @Test
    void shouldSkipDuplicateWebhookEventAlreadyInLedger() {
        IfoodIngestionService service = buildService(true, true);
        when(eventLedgerRepository.registerForProcessing(any())).thenReturn(RegistrationOutcome.DUPLICATE_PROCESSED);

        IfoodIngestionResponse response = service.ingestFromWebhook(
                "[{\"id\":\"evt-2\",\"code\":\"PLC\",\"orderId\":\"order-2\",\"merchantId\":\"merchant-2\"}]",
                null
        );

        assertThat(response.getProcessedEvents()).isEqualTo(1);
        assertThat(response.getImportedOrders()).isEqualTo(0);
        assertThat(response.getDuplicateOrders()).isEqualTo(1);
        assertThat(response.getFailedEvents()).isEqualTo(0);

        verify(ifoodClient, never()).fetchOrderById(any(), any());
        verify(canonicalOrderIngestBridge, never()).ingestIfoodPayload(any(), any(), any(), any());
        verify(eventLedgerRepository, never()).markProcessed(any());
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

        when(ifoodClient.fetchOrderById("merchant-3", "order-3")).thenReturn(payload);
        when(marketplaceOrderMapper.toCreateOrderRequest(payload)).thenReturn(request);
        when(canonicalOrderIngestBridge.ingestIfoodPayload(any(), any(), any(), any()))
                .thenReturn(OrderIngestResult.CREATED)
                .thenReturn(OrderIngestResult.DUPLICATE);

        String payloadJson = "[{\"id\":\"evt-3\",\"code\":\"PLC\",\"orderId\":\"order-3\",\"merchantId\":\"merchant-3\"}]";

        IfoodIngestionResponse first = service.ingestFromWebhook(payloadJson, null);
        IfoodIngestionResponse second = service.ingestFromWebhook(payloadJson, null);

        assertThat(first.getImportedOrders()).isEqualTo(1);
        assertThat(second.getImportedOrders()).isEqualTo(0);
        assertThat(second.getDuplicateOrders()).isEqualTo(1);

        verify(eventLedgerRepository, never()).registerForProcessing(any());
        verify(canonicalOrderIngestBridge, times(1)).ingestIfoodPayload(any(), any(), any(), any());
    }

    @Test
    void shouldAllowRetryForFailedEventWhenLedgerDisabled() {
        IfoodIngestionService service = buildService(false, false);

        MarketplaceOrderPayload payload = MarketplaceOrderPayload.builder()
                .source(OrderSource.IFOOD)
                .merchantId("merchant-4")
                .externalOrderId("order-4")
                .customerName("Cliente")
                .build();
        CreateOrderRequest request = CreateOrderRequest.builder().build();

        when(ifoodClient.fetchOrderById("merchant-4", "order-4")).thenReturn(payload);
        when(marketplaceOrderMapper.toCreateOrderRequest(payload))
                .thenThrow(new IllegalArgumentException("Unknown marketplace sku: 9184"))
                .thenReturn(request);
        when(canonicalOrderIngestBridge.ingestIfoodPayload(any(), any(), any(), any()))
                .thenReturn(OrderIngestResult.CREATED);

        String payloadJson = "[{\"id\":\"evt-4\",\"code\":\"PLC\",\"orderId\":\"order-4\",\"merchantId\":\"merchant-4\"}]";

        IfoodIngestionResponse first = service.ingestFromWebhook(payloadJson, null);
        IfoodIngestionResponse second = service.ingestFromWebhook(payloadJson, null);

        assertThat(first.getFailedEvents()).isEqualTo(1);
        assertThat(first.getImportedOrders()).isEqualTo(0);
        assertThat(second.getImportedOrders()).isEqualTo(1);
        assertThat(second.getDuplicateOrders()).isEqualTo(0);

        verify(canonicalOrderIngestBridge, times(1)).ingestIfoodPayload(any(), any(), any(), any());
    }

    @Test
    void shouldRetryFailedEventWhenDurableLedgerAllowsAcquireRetry() {
        IfoodIngestionService service = buildService(true, true);

        MarketplaceOrderPayload payload = MarketplaceOrderPayload.builder()
                .source(OrderSource.IFOOD)
                .merchantId("merchant-5")
                .externalOrderId("order-5")
                .customerName("Cliente")
                .build();
        CreateOrderRequest request = CreateOrderRequest.builder().build();

        when(eventLedgerRepository.registerForProcessing(any()))
                .thenReturn(RegistrationOutcome.ACQUIRED)
                .thenReturn(RegistrationOutcome.ACQUIRED_RETRY);
        when(ifoodClient.fetchOrderById("merchant-5", "order-5")).thenReturn(payload);
        when(marketplaceOrderMapper.toCreateOrderRequest(payload))
                .thenThrow(new IllegalArgumentException("Unknown marketplace sku: 9184"))
                .thenReturn(request);
        when(canonicalOrderIngestBridge.ingestIfoodPayload(any(), any(), any(), any()))
                .thenReturn(OrderIngestResult.CREATED);

        String payloadJson = "[{\"id\":\"evt-5\",\"code\":\"PLC\",\"orderId\":\"order-5\",\"merchantId\":\"merchant-5\"}]";

        IfoodIngestionResponse first = service.ingestFromWebhook(payloadJson, null);
        IfoodIngestionResponse second = service.ingestFromWebhook(payloadJson, null);

        assertThat(first.getFailedEvents()).isEqualTo(1);
        assertThat(second.getImportedOrders()).isEqualTo(1);

        verify(eventLedgerRepository, times(1)).markFailed(any(), any());
        verify(eventLedgerRepository, times(1)).markProcessed(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenWebhookSignatureIsMissing() {
        IfoodIngestionService service = buildService(false, false, "super-secret");

        assertThatThrownBy(() -> service.ingestFromWebhook("[]", null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException responseException = (ResponseStatusException) ex;
                    assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(responseException.getReason()).isEqualTo("Missing iFood webhook signature");
                });
    }

    @Test
    void shouldReturnUnauthorizedWhenWebhookSignatureIsInvalid() {
        IfoodIngestionService service = buildService(false, false, "super-secret");

        assertThatThrownBy(() -> service.ingestFromWebhook("[]", "invalid-signature"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException responseException = (ResponseStatusException) ex;
                    assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(responseException.getReason()).isEqualTo("Invalid iFood webhook signature");
                });
    }

    @Test
    void shouldReturnBadRequestWhenWebhookPayloadIsNotArray() {
        IfoodIngestionService service = buildService(false, false);

        assertThatThrownBy(() -> service.ingestFromWebhook("{\"id\":\"evt-6\"}", null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException responseException = (ResponseStatusException) ex;
                    assertThat(responseException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(responseException.getReason()).isEqualTo("iFood webhook payload must be a JSON array");
                });
    }

    private IfoodIngestionService buildService(boolean eventLedgerEnabled, boolean ledgerConfigured) {
        return buildService(eventLedgerEnabled, ledgerConfigured, null);
    }

    private IfoodIngestionService buildService(
            boolean eventLedgerEnabled,
            boolean ledgerConfigured,
            String webhookSecret
    ) {
        IfoodProperties properties = new IfoodProperties(
                true,
                "https://merchant-api.ifood.com.br",
                "client-id",
                "client-secret",
                true,
                webhookSecret,
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
                properties,
                eventLedgerRepository,
                new ObjectMapper(),
                canonicalOrderIngestBridge
        );
    }
}
