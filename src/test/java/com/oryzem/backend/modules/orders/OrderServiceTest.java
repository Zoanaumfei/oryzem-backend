package com.oryzem.backend.modules.orders;

import com.oryzem.backend.modules.catalog.domain.Product;
import com.oryzem.backend.modules.catalog.repository.ProductRepository;
import com.oryzem.backend.modules.inventory.domain.InsufficientStockException;
import com.oryzem.backend.modules.inventory.domain.InventoryMovementType;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementRequest;
import com.oryzem.backend.modules.inventory.repository.InventoryItemRepository;
import com.oryzem.backend.modules.inventory.repository.InventoryMovementRepository;
import com.oryzem.backend.modules.inventory.service.InventoryService;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderItemRequest;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.repository.OrderAuditEventRepository;
import com.oryzem.backend.modules.orders.repository.OrderRepository;
import com.oryzem.backend.modules.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceTest {

    private ProductRepository productRepository;
    private InventoryService inventoryService;
    private OrderRepository orderRepository;
    private OrderAuditEventRepository auditEventRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
        inventoryService = new InventoryService(
                productRepository,
                new InventoryItemRepository(),
                new InventoryMovementRepository()
        );
        orderRepository = new OrderRepository();
        auditEventRepository = new OrderAuditEventRepository();
        orderService = new OrderService(orderRepository, auditEventRepository, productRepository, inventoryService);
    }

    @Test
    void shouldDeductInventoryOnConfirmedOrder() {
        Product product = createProduct("PIZZA-CALABRESA", "Pizza Calabresa", new BigDecimal("59.90"));
        addStock(product.getId(), 10);

        OrderResponse created = orderService.createOrder(buildInternalOrder(product.getId(), 3));
        OrderResponse confirmed = orderService.confirmOrder(created.getId());

        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(confirmed.isStockAllocated()).isTrue();
        assertThat(inventoryService.getInventoryByProductId(product.getId()).getQuantityAvailable()).isEqualTo(7);
    }

    @Test
    void shouldBlockConfirmationWhenStockIsInsufficient() {
        Product product = createProduct("REFRI-COLA-2L", "Refrigerante Cola 2L", new BigDecimal("12.00"));
        addStock(product.getId(), 2);

        OrderResponse created = orderService.createOrder(buildInternalOrder(product.getId(), 5));

        assertThatThrownBy(() -> orderService.confirmOrder(created.getId()))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for product");

        OrderResponse storedOrder = orderService.getOrderById(created.getId());
        assertThat(storedOrder.getStatus()).isEqualTo(OrderStatus.ALLOCATION_ERROR);
        assertThat(storedOrder.getAllocationError()).contains("Insufficient stock");
        assertThat(inventoryService.getInventoryByProductId(product.getId()).getQuantityAvailable()).isEqualTo(2);
        assertThat(auditEventRepository.findByOrderId(created.getId())).hasSize(1);
    }

    @Test
    void shouldBeIdempotentWhenConfirmingOrderTwice() {
        Product product = createProduct("PIZZA-MARGHERITA", "Pizza Margherita", new BigDecimal("54.00"));
        addStock(product.getId(), 8);

        OrderResponse created = orderService.createOrder(buildInternalOrder(product.getId(), 2));

        OrderResponse firstConfirmation = orderService.confirmOrder(created.getId());
        OrderResponse secondConfirmation = orderService.confirmOrder(created.getId());

        assertThat(firstConfirmation.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(secondConfirmation.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(secondConfirmation.getMessage()).isEqualTo("Order already confirmed");
        assertThat(inventoryService.getInventoryByProductId(product.getId()).getQuantityAvailable()).isEqualTo(6);
        assertThat(inventoryService.getMovementsByOrderId(created.getId())).hasSize(1);
    }

    @Test
    void shouldAdvanceOrderLifecycleToCompleted() {
        Product product = createProduct("PASTA-BOLONHESA", "Pasta Bolonhesa", new BigDecimal("39.90"));
        addStock(product.getId(), 4);

        OrderResponse created = orderService.createOrder(buildInternalOrder(product.getId(), 1));
        OrderResponse confirmed = orderService.confirmOrder(created.getId());
        OrderResponse preparing = orderService.startPreparing(created.getId());
        OrderResponse dispatched = orderService.dispatchOrder(created.getId());
        OrderResponse completed = orderService.completeOrder(created.getId());

        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(preparing.getStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(dispatched.getStatus()).isEqualTo(OrderStatus.DISPATCHED);
        assertThat(completed.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void shouldRequirePreparingBeforeDispatch() {
        Product product = createProduct("LASANHA", "Lasanha", new BigDecimal("42.00"));
        addStock(product.getId(), 4);

        OrderResponse created = orderService.createOrder(buildInternalOrder(product.getId(), 1));
        orderService.confirmOrder(created.getId());

        assertThatThrownBy(() -> orderService.dispatchOrder(created.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be preparing before dispatch");
    }

    private Product createProduct(String sku, String name, BigDecimal unitPrice) {
        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .sku(sku)
                .name(name)
                .category("PIZZA")
                .unitPrice(unitPrice)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return productRepository.save(product);
    }

    private void addStock(String productId, int quantity) {
        inventoryService.applyMovement(InventoryMovementRequest.builder()
                .productId(productId)
                .type(InventoryMovementType.IN)
                .quantity(quantity)
                .reason("INITIAL_STOCK")
                .minimumLevel(1)
                .build());
    }

    private CreateOrderRequest buildInternalOrder(String productId, int quantity) {
        return CreateOrderRequest.builder()
                .source(OrderSource.INTERNAL)
                .customerName("Cliente Balcao")
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(productId)
                                .nameSnapshot("Produto " + productId)
                                .quantity(quantity)
                                .unitPrice(new BigDecimal("10.00"))
                                .build()
                ))
                .build();
    }
}
