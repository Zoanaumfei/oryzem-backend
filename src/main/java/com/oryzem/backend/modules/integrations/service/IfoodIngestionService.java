package com.oryzem.backend.modules.integrations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oryzem.backend.modules.integrations.config.IfoodProperties;
import com.oryzem.backend.modules.integrations.domain.IfoodEventLedgerEntry;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.integrations.dto.IfoodIngestionResponse;
import com.oryzem.backend.modules.integrations.repository.IfoodEventLedgerRepository;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class IfoodIngestionService {

    private final IfoodMarketplaceClient ifoodClient;
    private final MarketplaceOrderMapper marketplaceOrderMapper;
    private final OrderService orderService;
    private final IfoodProperties ifoodProperties;
    private final IfoodEventLedgerRepository eventLedgerRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, Instant> webhookEventLedger = new ConcurrentHashMap<>();
    private final AtomicBoolean eventLedgerConfigWarned = new AtomicBoolean(false);

    public IfoodIngestionResponse ingestFromPolling() {
        if (!ifoodProperties.enabled()) {
            return IfoodIngestionResponse.builder().build();
        }

        int imported = 0;
        int duplicates = 0;
        int failed = 0;
        int processed = 0;
        List<String> errors = new ArrayList<>();

        List<MarketplaceOrderPayload> payloads = ifoodClient.fetchNewOrders();
        for (MarketplaceOrderPayload payload : payloads) {
            processed++;
            try {
                OrderResponse response = createOrder(payload);
                if ("Order already exists".equals(response.getMessage())) {
                    duplicates++;
                } else {
                    imported++;
                }
                ifoodClient.ackOrder(payload.getMerchantId(), payload.getExternalOrderId());
            } catch (Exception ex) {
                failed++;
                String error = "Polling failed for " + payload.getMerchantId() + "/" + payload.getExternalOrderId()
                        + ": " + ex.getMessage();
                errors.add(error);
                log.warn(error, ex);
            }
        }

        return IfoodIngestionResponse.builder()
                .processedEvents(processed)
                .importedOrders(imported)
                .duplicateOrders(duplicates)
                .failedEvents(failed)
                .errors(errors)
                .build();
    }

    public IfoodIngestionResponse ingestFromWebhook(String rawBody, String signature) {
        if (!ifoodProperties.enabled() || !ifoodProperties.webhookEnabled()) {
            return IfoodIngestionResponse.builder().build();
        }
        validateSignature(rawBody, signature);

        int imported = 0;
        int duplicates = 0;
        int failed = 0;
        int processed = 0;
        List<String> errors = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            if (!root.isArray()) {
                throw new IllegalArgumentException("iFood webhook payload must be a JSON array");
            }

            for (JsonNode eventNode : root) {
                processed++;
                try {
                    IfoodWebhookEvent event = parseWebhookEvent(eventNode);
                    if (event == null || event.eventId() == null) {
                        continue;
                    }

                    if (!registerIfFirstSeen(event)) {
                        duplicates++;
                        continue;
                    }

                    if (!"PLC".equalsIgnoreCase(event.code())) {
                        continue;
                    }

                    MarketplaceOrderPayload payload = ifoodClient.fetchOrderById(event.merchantId(), event.orderId());
                    OrderResponse response = createOrder(payload);
                    if ("Order already exists".equals(response.getMessage())) {
                        duplicates++;
                    } else {
                        imported++;
                    }
                } catch (Exception ex) {
                    failed++;
                    String error = "Webhook failed for event: " + ex.getMessage();
                    errors.add(error);
                    log.warn(error, ex);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid iFood webhook payload: " + ex.getMessage(), ex);
        }

        return IfoodIngestionResponse.builder()
                .processedEvents(processed)
                .importedOrders(imported)
                .duplicateOrders(duplicates)
                .failedEvents(failed)
                .errors(errors)
                .build();
    }

    private OrderResponse createOrder(MarketplaceOrderPayload payload) {
        CreateOrderRequest request = marketplaceOrderMapper.toCreateOrderRequest(payload);
        return orderService.createOrder(request);
    }

    private boolean registerIfFirstSeen(IfoodWebhookEvent event) {
        if (ifoodProperties.eventLedgerEnabled()) {
            if (!eventLedgerRepository.isConfigured()) {
                if (eventLedgerConfigWarned.compareAndSet(false, true)) {
                    log.warn("iFood event ledger is enabled but app.dynamodb.tables.ifood-event-ledger is not configured. Falling back to in-memory dedupe.");
                }
                return registerInMemory(event);
            }

            try {
                return eventLedgerRepository.saveIfAbsent(toLedgerEntry(event));
            } catch (Exception ex) {
                log.warn("Failed to persist iFood event in ledger. Falling back to in-memory dedupe: {}", ex.getMessage());
                return registerInMemory(event);
            }
        }
        return registerInMemory(event);
    }

    private boolean registerInMemory(IfoodWebhookEvent event) {
        String ledgerKey = event.merchantId() + "::" + event.eventId();
        return webhookEventLedger.putIfAbsent(ledgerKey, Instant.now()) == null;
    }

    private IfoodEventLedgerEntry toLedgerEntry(IfoodWebhookEvent event) {
        long processedAtEpochSeconds = Instant.now().getEpochSecond();
        long expiresAtEpochSeconds = processedAtEpochSeconds + (long) ifoodProperties.eventLedgerTtlDays() * 24L * 60L * 60L;

        IfoodEventLedgerEntry entry = new IfoodEventLedgerEntry();
        entry.setMerchantId(event.merchantId());
        entry.setEventId(event.eventId());
        entry.setChannel("WEBHOOK");
        entry.setOrderId(event.orderId());
        entry.setEventCode(event.code());
        entry.setSource("IFOOD");
        entry.setProcessedAtEpochSeconds(processedAtEpochSeconds);
        entry.setExpiresAtEpochSeconds(expiresAtEpochSeconds);
        return entry;
    }

    private IfoodWebhookEvent parseWebhookEvent(JsonNode eventNode) {
        String eventId = text(eventNode, "id");
        String code = text(eventNode, "code");
        String orderId = text(eventNode, "orderId");
        String merchantId = firstNonBlank(
                text(eventNode, "merchantId"),
                text(eventNode.path("merchant"), "id")
        );
        if (eventId == null || code == null || orderId == null || merchantId == null) {
            return null;
        }
        return new IfoodWebhookEvent(eventId, code, orderId, merchantId);
    }

    private void validateSignature(String rawBody, String receivedSignature) {
        String secret = ifoodProperties.webhookSecret();
        if (secret == null || secret.isBlank()) {
            return;
        }
        if (receivedSignature == null || receivedSignature.isBlank()) {
            throw new IllegalArgumentException("Missing iFood webhook signature");
        }

        String normalized = receivedSignature.trim();
        int idx = normalized.indexOf('=');
        if (idx >= 0 && idx < normalized.length() - 1) {
            normalized = normalized.substring(idx + 1);
        }
        normalized = normalized.trim();

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));

            String expectedHex = toHex(digest);
            String expectedBase64 = Base64.getEncoder().encodeToString(digest);

            boolean valid = MessageDigest.isEqual(
                    normalized.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),
                    expectedHex.getBytes(StandardCharsets.UTF_8)
            ) || MessageDigest.isEqual(
                    normalized.getBytes(StandardCharsets.UTF_8),
                    expectedBase64.getBytes(StandardCharsets.UTF_8)
            );
            if (!valid) {
                throw new IllegalArgumentException("Invalid iFood webhook signature");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to validate iFood webhook signature: " + ex.getMessage(), ex);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String text(JsonNode node, String fieldName) {
        JsonNode child = node.path(fieldName);
        if (child.isMissingNode() || child.isNull()) {
            return null;
        }
        String value = child.asText();
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private record IfoodWebhookEvent(String eventId, String code, String orderId, String merchantId) {
    }
}
