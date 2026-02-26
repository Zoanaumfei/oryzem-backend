package com.oryzem.backend.modules.integrations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oryzem.backend.modules.integrations.config.IfoodProperties;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderItemPayload;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Slf4j
@RequiredArgsConstructor
public class IfoodMarketplaceClient implements MarketplaceClient {

    private static final String AUTH_PATH = "/authentication/v1.0/oauth/token";
    private static final String POLLING_PATH = "/events/v1.0/events:polling";
    private static final String ACK_PATH = "/events/v1.0/events/acknowledgment";
    private static final String ORDER_DETAILS_PATH = "/order/v1.0/orders/";
    private static final String ORDER_CANCELLATION_REASONS_SUFFIX = "/cancellationReasons";
    private static final String ORDER_REQUEST_CANCELLATION_SUFFIX = "/requestCancellation";
    private static final String DEFAULT_CUSTOMER_NAME = "Cliente iFood";

    private final ObjectMapper objectMapper;
    private final IfoodProperties properties;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Object tokenLock = new Object();
    private final Map<String, Deque<String>> pendingEventIdsByOrderKey = new ConcurrentHashMap<>();

    private volatile String cachedAccessToken;
    private volatile Instant cachedAccessTokenExpiresAt = Instant.EPOCH;

    @Override
    public OrderSource supportedSource() {
        return OrderSource.IFOOD;
    }

    @Override
    public List<MarketplaceOrderPayload> fetchNewOrders() {
        if (!properties.enabled()) {
            return List.of();
        }

        if (!properties.hasRequiredCredentials()) {
            log.warn("iFood integration is enabled but credentials are missing. Skipping polling.");
            return List.of();
        }

        try {
            String accessToken = getAccessToken();
            List<IfoodEvent> events = pollEvents(accessToken);
            if (events.isEmpty()) {
                return List.of();
            }

            List<MarketplaceOrderPayload> payloads = new ArrayList<>();
            for (IfoodEvent event : events) {
                try {
                    MarketplaceOrderPayload payload = fetchOrderById(accessToken, event.merchantId(), event.orderId());
                    payloads.add(payload);
                    enqueueAckEvent(payload.getMerchantId(), payload.getExternalOrderId(), event.id());
                } catch (Exception ex) {
                    log.warn(
                            "Failed to map iFood order {}/{} from event {}: {}",
                            event.merchantId(),
                            event.orderId(),
                            event.id(),
                            ex.getMessage()
                    );
                }
            }
            return payloads;
        } catch (Exception ex) {
            log.error("Failed to fetch iFood orders: {}", ex.getMessage());
            return List.of();
        }
    }

    public MarketplaceOrderPayload fetchOrderById(String merchantId, String externalOrderId) {
        if (!properties.enabled()) {
            throw new IllegalStateException("iFood integration is disabled");
        }
        if (!properties.hasRequiredCredentials()) {
            throw new IllegalStateException("iFood credentials are required");
        }
        if (externalOrderId == null || externalOrderId.isBlank()) {
            throw new IllegalArgumentException("iFood externalOrderId is required");
        }
        try {
            return fetchOrderById(getAccessToken(), merchantId, externalOrderId);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to fetch iFood order " + externalOrderId + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public void ackOrder(String merchantId, String externalOrderId) {
        if (!properties.enabled()
                || merchantId == null
                || merchantId.isBlank()
                || externalOrderId == null
                || externalOrderId.isBlank()) {
            return;
        }

        String orderKey = toOrderKey(merchantId, externalOrderId);
        Deque<String> pendingAcks = pendingEventIdsByOrderKey.get(orderKey);
        if (pendingAcks == null) {
            log.debug("No pending iFood ack found for order {}/{}", merchantId, externalOrderId);
            return;
        }

        String eventId = pendingAcks.pollFirst();
        if (eventId == null) {
            pendingEventIdsByOrderKey.remove(orderKey, pendingAcks);
            return;
        }

        try {
            String body = objectMapper.writeValueAsString(
                    List.of(Collections.singletonMap("id", eventId))
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildUri(ACK_PATH))
                    .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 202 || status == 200) {
                log.info("iFood ack success for event {} (merchant={} order={})", eventId, merchantId, externalOrderId);
                if (pendingAcks.isEmpty()) {
                    pendingEventIdsByOrderKey.remove(orderKey, pendingAcks);
                }
            } else {
                pendingAcks.offerFirst(eventId);
                log.warn("iFood ack failed for event {} (merchant={} order={}): status={} body={}",
                        eventId, merchantId, externalOrderId, status, response.body());
            }
        } catch (Exception ex) {
            pendingAcks.offerFirst(eventId);
            log.warn("iFood ack failed for event {} (merchant={} order={}): {}",
                    eventId, merchantId, externalOrderId, ex.getMessage());
        }
    }

    @Override
    public void updateOrderStatus(String merchantId, String externalOrderId, OrderStatus status) {
        if (!properties.enabled()
                || merchantId == null
                || merchantId.isBlank()
                || externalOrderId == null
                || externalOrderId.isBlank()
                || status == null) {
            return;
        }

        String command = switch (status) {
            case CONFIRMED -> "confirm";
            case PREPARING -> "startPreparation";
            case DISPATCHED -> "dispatch";
            case COMPLETED -> null;
            case CANCELED -> null;
            case RECEIVED -> null;
            case ALLOCATION_ERROR -> null;
        };

        if (status == OrderStatus.CANCELED) {
            requestCancellation(merchantId, externalOrderId, status);
            return;
        }

        if (command == null) {
            if (status == OrderStatus.COMPLETED) {
                log.info(
                        "Skipping iFood status update for order {}: completed is handled by iFood delivery flow",
                        merchantId + "/" + externalOrderId
                );
            } else {
                log.debug("Skipping iFood status update for order {}/{} status {}", merchantId, externalOrderId, status);
            }
            return;
        }

        try {
            String body = "{}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildUri(ORDER_DETAILS_PATH + urlEncode(externalOrderId) + "/" + command))
                    .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseStatus = response.statusCode();
            if (responseStatus == 202 || responseStatus == 200 || responseStatus == 409) {
                log.info(
                        "iFood status update success for order {}/{} -> {} (http={})",
                        merchantId,
                        externalOrderId,
                        status,
                        responseStatus
                );
                return;
            }

            log.warn(
                    "iFood status update failed for order {}/{} -> {} (http={} body={})",
                    merchantId,
                    externalOrderId,
                    status,
                    responseStatus,
                    response.body()
            );
        } catch (Exception ex) {
            log.warn(
                    "iFood status update error for order {}/{} -> {}: {}",
                    merchantId,
                    externalOrderId,
                    status,
                    ex.getMessage()
            );
        }
    }

    private void requestCancellation(String merchantId, String externalOrderId, OrderStatus status) {
        try {
            String accessToken = getAccessToken();
            String cancellationCode = resolveCancellationCode(merchantId, externalOrderId, accessToken);
            if (cancellationCode == null) {
                log.warn(
                        "iFood cancellation not sent for order {}/{}: no cancellation code available",
                        merchantId,
                        externalOrderId
                );
                return;
            }

            Map<String, String> requestBody = Map.of(
                    "reason", properties.cancellationReason(),
                    "cancellationCode", cancellationCode
            );
            String body = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildUri(ORDER_DETAILS_PATH + urlEncode(externalOrderId) + ORDER_REQUEST_CANCELLATION_SUFFIX))
                    .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseStatus = response.statusCode();
            if (responseStatus == 202 || responseStatus == 200 || responseStatus == 409) {
                log.info(
                        "iFood status update success for order {}/{} -> {} (http={} code={})",
                        merchantId,
                        externalOrderId,
                        status,
                        responseStatus,
                        cancellationCode
                );
                return;
            }

            log.warn(
                    "iFood cancellation failed for order {}/{} (http={} body={})",
                    merchantId,
                    externalOrderId,
                    responseStatus,
                    response.body()
            );
        } catch (Exception ex) {
            log.warn("iFood cancellation error for order {}/{}: {}", merchantId, externalOrderId, ex.getMessage());
        }
    }

    private List<IfoodEvent> pollEvents(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildPollingUri())
                .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .header("Authorization", "Bearer " + accessToken)
                .headers(buildPollingHeaders())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status == 204) {
            return List.of();
        }
        if (status != 200) {
            throw new IllegalStateException("Polling failed with status " + status + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        if (!root.isArray()) {
            return List.of();
        }

        List<IfoodEvent> events = new ArrayList<>();
        for (JsonNode node : root) {
            String eventId = text(node, "id");
            String orderId = text(node, "orderId");
            String merchantId = firstNonBlank(
                    text(node, "merchantId"),
                    text(node.path("merchant"), "id")
            );
            if (eventId == null || orderId == null) {
                continue;
            }
            events.add(new IfoodEvent(eventId, merchantId, orderId));
        }
        return events;
    }

    private MarketplaceOrderPayload fetchOrderById(String accessToken, String merchantId, String requestedOrderId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(ORDER_DETAILS_PATH + urlEncode(requestedOrderId)))
                .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status != 200) {
            throw new IllegalStateException("Order details failed with status " + status + ": " + response.body());
        }

        JsonNode orderNode = objectMapper.readTree(response.body());
        String resolvedExternalOrderId = text(orderNode, "id");
        if (resolvedExternalOrderId == null) {
            resolvedExternalOrderId = requestedOrderId;
        }
        String resolvedMerchantId = resolveMerchantId(orderNode, merchantId);
        if (resolvedMerchantId == null) {
            throw new IllegalArgumentException("iFood order does not include merchantId");
        }

        List<MarketplaceOrderItemPayload> items = parseItems(orderNode.path("items"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("iFood order has no valid items");
        }

        return MarketplaceOrderPayload.builder()
                .source(OrderSource.IFOOD)
                .merchantId(resolvedMerchantId)
                .externalOrderId(resolvedExternalOrderId)
                .customerName(resolveCustomerName(orderNode))
                .items(items)
                .build();
    }

    private List<MarketplaceOrderItemPayload> parseItems(JsonNode itemsNode) {
        if (!itemsNode.isArray()) {
            return List.of();
        }

        List<MarketplaceOrderItemPayload> items = new ArrayList<>();
        for (JsonNode itemNode : itemsNode) {
            int quantity = intValue(itemNode, "quantity", 0);
            if (quantity <= 0) {
                continue;
            }

            String sku = firstNonBlank(
                    text(itemNode, "externalCode"),
                    text(itemNode, "id")
            );
            if (sku == null) {
                continue;
            }

            MarketplaceOrderItemPayload payload = MarketplaceOrderItemPayload.builder()
                    .sku(sku)
                    .name(firstNonBlank(text(itemNode, "name"), "Item iFood"))
                    .quantity(quantity)
                    .unitPrice(resolveUnitPrice(itemNode))
                    .build();
            items.add(payload);
        }
        return items;
    }

    private BigDecimal resolveUnitPrice(JsonNode itemNode) {
        BigDecimal unitPrice = firstNonNull(
                decimal(itemNode, "unitPrice"),
                decimal(itemNode.path("price"), "value")
        );
        if (unitPrice != null) {
            return unitPrice;
        }

        BigDecimal totalPrice = firstNonNull(
                decimal(itemNode, "totalPrice"),
                decimal(itemNode.path("total"), "value")
        );
        int quantity = intValue(itemNode, "quantity", 1);
        if (totalPrice != null && quantity > 0) {
            return totalPrice.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private String resolveCustomerName(JsonNode orderNode) {
        return firstNonBlank(
                text(orderNode.path("customer"), "name"),
                text(orderNode.path("delivery").path("deliveryAddress"), "reference"),
                DEFAULT_CUSTOMER_NAME
        );
    }

    private String getAccessToken() throws Exception {
        Instant now = Instant.now();
        if (cachedAccessToken != null && now.isBefore(cachedAccessTokenExpiresAt.minusSeconds(properties.tokenRefreshSkewSeconds()))) {
            return cachedAccessToken;
        }

        synchronized (tokenLock) {
            now = Instant.now();
            if (cachedAccessToken != null && now.isBefore(cachedAccessTokenExpiresAt.minusSeconds(properties.tokenRefreshSkewSeconds()))) {
                return cachedAccessToken;
            }

            String formBody = "grantType=client_credentials"
                    + "&clientId=" + urlEncode(properties.clientId())
                    + "&clientSecret=" + urlEncode(properties.clientSecret());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildUri(AUTH_PATH))
                    .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "iFood auth failed with status " + response.statusCode() + ": " + response.body()
                );
            }

            JsonNode tokenNode = objectMapper.readTree(response.body());
            String accessToken = text(tokenNode, "accessToken");
            if (accessToken == null) {
                throw new IllegalStateException("iFood auth response did not include accessToken");
            }

            long expiresIn = longValue(tokenNode, "expiresIn", 21600L);
            cachedAccessToken = accessToken;
            cachedAccessTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
            return cachedAccessToken;
        }
    }

    private URI buildPollingUri() {
        StringBuilder path = new StringBuilder(POLLING_PATH);
        if (!properties.eventTypes().isEmpty()) {
            path.append("?types=").append(urlEncode(String.join(",", properties.eventTypes())));
        }
        return buildUri(path.toString());
    }

    private String resolveCancellationCode(String merchantId, String externalOrderId, String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(ORDER_DETAILS_PATH + urlEncode(externalOrderId) + ORDER_CANCELLATION_REASONS_SUFFIX))
                .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Cancellation reasons failed for merchant " + merchantId
                            + " with status " + response.statusCode() + ": " + response.body()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());
        if (!root.isArray() || root.isEmpty()) {
            return null;
        }

        String preferredCode = properties.preferredCancellationCode();
        String fallbackCode = null;
        for (JsonNode reasonNode : root) {
            String code = firstNonBlank(
                    text(reasonNode, "cancellationCode"),
                    text(reasonNode, "cancelCodeId"),
                    text(reasonNode, "code"),
                    text(reasonNode, "id")
            );
            if (code == null) {
                continue;
            }

            if (preferredCode != null && preferredCode.equalsIgnoreCase(code)) {
                return code;
            }
            if (fallbackCode == null) {
                fallbackCode = code;
            }
        }
        return fallbackCode;
    }

    private String[] buildPollingHeaders() {
        if (properties.merchantIds().isEmpty()) {
            return new String[0];
        }
        return new String[]{"x-polling-merchants", String.join(",", properties.merchantIds())};
    }

    private URI buildUri(String path) {
        String baseUrl = properties.baseUrl();
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return URI.create(baseUrl.substring(0, baseUrl.length() - 1) + path);
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return URI.create(baseUrl + "/" + path);
        }
        return URI.create(baseUrl + path);
    }

    private String resolveMerchantId(JsonNode orderNode, String eventMerchantId) {
        return firstNonBlank(
                text(orderNode.path("merchant"), "id"),
                text(orderNode, "merchantId"),
                eventMerchantId,
                properties.merchantIds().size() == 1 ? properties.merchantIds().get(0) : null
        );
    }

    private void enqueueAckEvent(String merchantId, String externalOrderId, String eventId) {
        String orderKey = toOrderKey(merchantId, externalOrderId);
        pendingEventIdsByOrderKey
                .computeIfAbsent(orderKey, ignored -> new ConcurrentLinkedDeque<>())
                .offerLast(eventId);
    }

    private String toOrderKey(String merchantId, String externalOrderId) {
        return merchantId.trim() + "::" + externalOrderId.trim();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(Objects.requireNonNullElse(value, ""), StandardCharsets.UTF_8);
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
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static int intValue(JsonNode node, String fieldName, int fallback) {
        JsonNode child = node.path(fieldName);
        return child.isNumber() ? child.asInt() : fallback;
    }

    private static long longValue(JsonNode node, String fieldName, long fallback) {
        JsonNode child = node.path(fieldName);
        return child.isNumber() ? child.asLong() : fallback;
    }

    private static BigDecimal decimal(JsonNode node, String fieldName) {
        JsonNode child = node.path(fieldName);
        if (child.isNumber()) {
            return child.decimalValue();
        }
        if (child.isTextual()) {
            try {
                return new BigDecimal(child.asText().trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private record IfoodEvent(String id, String merchantId, String orderId) {
    }
}
