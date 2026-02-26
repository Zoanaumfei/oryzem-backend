package com.oryzem.backend.modules.sync;

import com.oryzem.backend.modules.catalog.domain.Product;
import com.oryzem.backend.modules.catalog.repository.ProductRepository;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderItemPayload;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.integrations.service.MarketplaceClient;
import com.oryzem.backend.modules.integrations.service.MarketplaceOrderMapper;
import com.oryzem.backend.modules.inventory.repository.InventoryItemRepository;
import com.oryzem.backend.modules.inventory.repository.InventoryMovementRepository;
import com.oryzem.backend.modules.inventory.service.InventoryService;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import com.oryzem.backend.modules.orders.repository.OrderAuditEventRepository;
import com.oryzem.backend.modules.orders.repository.OrderRepository;
import com.oryzem.backend.modules.orders.service.OrderService;
import com.oryzem.backend.modules.sync.dto.OrderSyncResponse;
import com.oryzem.backend.modules.sync.service.OrderSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSyncServiceTest {

    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private OrderSyncService orderSyncService;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
        orderRepository = new OrderRepository();

        InventoryService inventoryService = new InventoryService(
                productRepository,
                new InventoryItemRepository(),
                new InventoryMovementRepository()
        );
        OrderService orderService = new OrderService(
                orderRepository,
                new OrderAuditEventRepository(),
                productRepository,
                inventoryService
        );
        MarketplaceOrderMapper mapper = new MarketplaceOrderMapper(productRepository);

        createProduct("PIZZA-CALABRESA", "Pizza Calabresa", new BigDecimal("59.90"));
        createProduct("REFRI-COLA-2L", "Refrigerante Cola 2L", new BigDecimal("12.00"));

        List<MarketplaceClient> clients = List.of(
                new StubMarketplaceClient(
                        OrderSource.IFOOD,
                        "IFOOD-MERCHANT-A",
                        "IFOOD-STUB-1001",
                        "PIZZA-CALABRESA",
                        "Pizza Calabresa"
                ),
                new StubMarketplaceClient(
                        OrderSource.NINENINE,
                        "99FOOD-MERCHANT-A",
                        "99FOOD-STUB-2001",
                        "REFRI-COLA-2L",
                        "Refrigerante Cola 2L"
                )
        );
        orderSyncService = new OrderSyncService(clients, mapper, orderService);
    }

    @Test
    void shouldImportAndNormalizeMarketplaceOrders() {
        OrderSyncResponse firstSync = orderSyncService.syncOrders();
        assertThat(firstSync.getImportedCount()).isEqualTo(2);
        assertThat(firstSync.getDuplicateCount()).isEqualTo(0);
        assertThat(firstSync.getFailedCount()).isEqualTo(0);
        assertThat(orderRepository.findAll()).hasSize(2);
        assertThat(orderRepository.findAll())
                .extracting(order -> order.getSource())
                .containsExactlyInAnyOrder(OrderSource.IFOOD, OrderSource.NINENINE);

        OrderSyncResponse secondSync = orderSyncService.syncOrders();
        assertThat(secondSync.getImportedCount()).isEqualTo(0);
        assertThat(secondSync.getDuplicateCount()).isEqualTo(2);
        assertThat(secondSync.getFailedCount()).isEqualTo(0);
    }

    @Test
    void shouldKeepOrdersSeparatedByMerchantWhenExternalIdMatches() {
        List<MarketplaceClient> clients = List.of(
                new StubMarketplaceClient(
                        OrderSource.IFOOD,
                        "IFOOD-MERCHANT-X",
                        "IFOOD-SAME-EXTERNAL",
                        "PIZZA-CALABRESA",
                        "Pizza Calabresa"
                ),
                new StubMarketplaceClient(
                        OrderSource.IFOOD,
                        "IFOOD-MERCHANT-Y",
                        "IFOOD-SAME-EXTERNAL",
                        "PIZZA-CALABRESA",
                        "Pizza Calabresa"
                )
        );
        InventoryService inventoryService = new InventoryService(
                productRepository,
                new InventoryItemRepository(),
                new InventoryMovementRepository()
        );
        OrderService localOrderService = new OrderService(
                orderRepository,
                new OrderAuditEventRepository(),
                productRepository,
                inventoryService
        );
        MarketplaceOrderMapper localMapper = new MarketplaceOrderMapper(productRepository);
        OrderSyncService localSyncService = new OrderSyncService(clients, localMapper, localOrderService);

        OrderSyncResponse response = localSyncService.syncOrders();
        assertThat(response.getImportedCount()).isEqualTo(2);
        assertThat(response.getDuplicateCount()).isEqualTo(0);
        assertThat(orderRepository.findAll())
                .extracting(order -> order.getMerchantId())
                .containsExactlyInAnyOrder("IFOOD-MERCHANT-X", "IFOOD-MERCHANT-Y");
    }

    private Product createProduct(String sku, String name, BigDecimal price) {
        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .sku(sku)
                .name(name)
                .category("DEFAULT")
                .unitPrice(price)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return productRepository.save(product);
    }

    private static final class StubMarketplaceClient implements MarketplaceClient {

        private final OrderSource source;
        private final String merchantId;
        private final String externalOrderId;
        private final String sku;
        private final String itemName;
        private final List<String> acknowledgements = new ArrayList<>();

        private StubMarketplaceClient(
                OrderSource source,
                String merchantId,
                String externalOrderId,
                String sku,
                String itemName
        ) {
            this.source = source;
            this.merchantId = merchantId;
            this.externalOrderId = externalOrderId;
            this.sku = sku;
            this.itemName = itemName;
        }

        @Override
        public List<MarketplaceOrderPayload> fetchNewOrders() {
            return List.of(MarketplaceOrderPayload.builder()
                    .source(source)
                    .merchantId(merchantId)
                    .externalOrderId(externalOrderId)
                    .customerName("Marketplace Customer")
                    .items(List.of(MarketplaceOrderItemPayload.builder()
                            .sku(sku)
                            .name(itemName)
                            .quantity(1)
                            .unitPrice(new BigDecimal("10.00"))
                            .build()))
                    .build());
        }

        @Override
        public void ackOrder(String merchantId, String externalOrderId) {
            acknowledgements.add(merchantId + "::" + externalOrderId);
        }

        @Override
        public void updateOrderStatus(String merchantId, String externalOrderId, OrderStatus status) {
            // Not required for this test.
        }
    }
}
