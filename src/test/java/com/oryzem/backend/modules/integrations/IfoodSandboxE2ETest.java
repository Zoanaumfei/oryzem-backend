package com.oryzem.backend.modules.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oryzem.backend.modules.catalog.domain.Product;
import com.oryzem.backend.modules.catalog.repository.ProductRepository;
import com.oryzem.backend.modules.integrations.config.IfoodProperties;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderItemPayload;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.integrations.service.IfoodMarketplaceClient;
import com.oryzem.backend.modules.integrations.service.MarketplaceOrderMapper;
import com.oryzem.backend.modules.integrations.service.MarketplaceStatusSyncService;
import com.oryzem.backend.modules.inventory.domain.InventoryMovementType;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementRequest;
import com.oryzem.backend.modules.inventory.repository.InventoryItemRepository;
import com.oryzem.backend.modules.inventory.repository.InventoryMovementRepository;
import com.oryzem.backend.modules.inventory.service.InventoryService;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderItemResponse;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.repository.OrderAuditEventRepository;
import com.oryzem.backend.modules.orders.repository.OrderRepository;
import com.oryzem.backend.modules.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "IFOOD_E2E_ENABLED", matches = "true")
class IfoodSandboxE2ETest {

    @Test
    void shouldImportAndDriveStatusFlowAgainstIfoodSandbox() throws Exception {
        String clientId = requiredEnv("IFOOD_CLIENT_ID");
        String clientSecret = requiredEnv("IFOOD_CLIENT_SECRET");
        String merchantId = requiredEnv("IFOOD_MERCHANT_IDS");

        ProductRepository productRepository = new ProductRepository();
        InventoryService inventoryService = new InventoryService(
                productRepository,
                new InventoryItemRepository(),
                new InventoryMovementRepository()
        );
        OrderService orderService = new OrderService(
                new OrderRepository(),
                new OrderAuditEventRepository(),
                productRepository,
                inventoryService
        );

        IfoodProperties properties = new IfoodProperties(
                true,
                "https://merchant-api.ifood.com.br",
                clientId,
                clientSecret,
                false,
                null,
                false,
                7,
                false,
                300,
                splitCsv(merchantId),
                List.of("PLC"),
                null,
                "E2E cancelamento solicitado pela integracao",
                20,
                60
        );
        IfoodMarketplaceClient ifoodClient = new IfoodMarketplaceClient(new ObjectMapper(), properties);
        injectStatusSync(orderService, new MarketplaceStatusSyncService(List.of(ifoodClient)));
        MarketplaceOrderMapper mapper = new MarketplaceOrderMapper(productRepository);

        List<MarketplaceOrderPayload> payloads = ifoodClient.fetchNewOrders();
        assertThat(payloads).isNotEmpty();

        List<OrderResponse> importedOrders = new ArrayList<>();
        for (MarketplaceOrderPayload payload : payloads) {
            ensureProductsExist(productRepository, payload.getItems());
            CreateOrderRequest request = mapper.toCreateOrderRequest(payload);
            OrderResponse response = orderService.createOrder(request);
            ifoodClient.ackOrder(payload.getMerchantId(), payload.getExternalOrderId());
            importedOrders.add(response);
        }

        OrderResponse target = importedOrders.get(0);
        for (OrderItemResponse item : target.getItems()) {
            inventoryService.applyMovement(InventoryMovementRequest.builder()
                    .productId(item.getProductId())
                    .type(InventoryMovementType.IN)
                    .quantity(Math.max(5, item.getQuantity() * 2))
                    .reason("IFOOD_E2E_INITIAL_STOCK")
                    .minimumLevel(1)
                    .build());
        }

        OrderResponse confirmed = orderService.confirmOrder(target.getId());
        OrderResponse preparing = orderService.startPreparing(target.getId());
        OrderResponse dispatched = orderService.dispatchOrder(target.getId());
        OrderResponse canceled = orderService.cancelOrder(target.getId());

        assertThat(confirmed.getStatus().name()).isEqualTo("CONFIRMED");
        assertThat(preparing.getStatus().name()).isEqualTo("PREPARING");
        assertThat(dispatched.getStatus().name()).isEqualTo("DISPATCHED");
        assertThat(canceled.getStatus().name()).isEqualTo("CANCELED");
    }

    private void ensureProductsExist(ProductRepository productRepository, List<MarketplaceOrderItemPayload> items) {
        for (MarketplaceOrderItemPayload item : items) {
            String sku = item.getSku();
            if (sku == null || sku.isBlank()) {
                continue;
            }
            if (productRepository.findBySku(sku).isPresent()) {
                continue;
            }

            Product product = Product.builder()
                    .id(UUID.randomUUID().toString())
                    .sku(sku)
                    .name(item.getName() == null || item.getName().isBlank() ? "Produto iFood " + sku : item.getName())
                    .category("IFOOD")
                    .unitPrice(item.getUnitPrice() == null ? new BigDecimal("10.00") : item.getUnitPrice())
                    .active(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            productRepository.save(product);
        }
    }

    private void injectStatusSync(OrderService orderService, MarketplaceStatusSyncService statusSyncService) throws Exception {
        Field field = OrderService.class.getDeclaredField("marketplaceStatusSyncService");
        field.setAccessible(true);
        field.set(orderService, statusSyncService);
    }

    private String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required env var: " + key);
        }
        return value.trim();
    }

    private List<String> splitCsv(String value) {
        String[] parts = value.split(",");
        List<String> items = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                items.add(trimmed);
            }
        }
        return items;
    }
}
