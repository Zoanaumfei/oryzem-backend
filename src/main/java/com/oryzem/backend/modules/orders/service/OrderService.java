package com.oryzem.backend.modules.orders.service;

import com.oryzem.backend.modules.catalog.domain.Product;
import com.oryzem.backend.modules.catalog.repository.ProductRepository;
import com.oryzem.backend.modules.inventory.domain.InsufficientStockException;
import com.oryzem.backend.modules.inventory.service.InventoryService;
import com.oryzem.backend.modules.integrations.service.MarketplaceStatusSyncService;
import com.oryzem.backend.modules.orders.domain.Order;
import com.oryzem.backend.modules.orders.domain.OrderAuditEvent;
import com.oryzem.backend.modules.orders.domain.OrderItem;
import com.oryzem.backend.modules.orders.domain.OrderNotFoundException;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import com.oryzem.backend.modules.orders.domain.OrderStatus;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderItemRequest;
import com.oryzem.backend.modules.orders.dto.OrderItemResponse;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.repository.OrderAuditEventRepository;
import com.oryzem.backend.modules.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderAuditEventRepository orderAuditEventRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    @Autowired(required = false)
    private MarketplaceStatusSyncService marketplaceStatusSyncService;

    public OrderResponse createOrder(CreateOrderRequest request) {
        OrderSource source = request.getSource();
        if (source == null) {
            throw new IllegalArgumentException("source is required");
        }

        String merchantId = normalizeMerchantId(source, request.getMerchantId());
        String externalId = normalizeExternalId(source, request.getExternalId());
        if (source != OrderSource.INTERNAL) {
            Order existingOrder = orderRepository.findByExternalId(source, merchantId, externalId).orElse(null);
            if (existingOrder != null) {
                return toResponse(existingOrder, "Order already exists");
            }
        }

        List<OrderItem> items = normalizeItems(request.getItems());
        Instant now = Instant.now();
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .source(source)
                .merchantId(merchantId)
                .externalId(externalId)
                .customerName(normalizeRequired(request.getCustomerName(), "customerName"))
                .items(items)
                .totalAmount(calculateTotal(items))
                .status(OrderStatus.RECEIVED)
                .createdAt(now)
                .updatedAt(now)
                .stockAllocated(false)
                .allocationError(null)
                .build();

        Order saved = orderRepository.save(order);
        return toResponse(saved, "Order created successfully");
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = findOrder(orderId);
        return toResponse(order, "Order found");
    }

    public OrderResponse confirmOrder(String orderId) {
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Canceled order cannot be confirmed");
        }

        if (order.isStockAllocated()) {
            return toResponse(order, "Order already confirmed");
        }

        try {
            inventoryService.reserveStockForOrder(order.getId(), order.getItems());
        } catch (InsufficientStockException ex) {
            Order allocationErrorOrder = updateOrder(order, OrderStatus.ALLOCATION_ERROR, false, ex.getMessage());
            registerAuditEvent(allocationErrorOrder.getId(), "ALLOCATION_FAILED", ex.getMessage());
            throw ex;
        }

        Order confirmedOrder = updateOrder(order, OrderStatus.CONFIRMED, true, null);
        publishMarketplaceStatus(confirmedOrder, OrderStatus.CONFIRMED);
        return toResponse(confirmedOrder, "Order confirmed");
    }

    public OrderResponse cancelOrder(String orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELED) {
            return toResponse(order, "Order already canceled");
        }

        if (order.isStockAllocated()) {
            inventoryService.releaseStockForOrder(order.getId(), order.getItems());
        }

        Order canceledOrder = updateOrder(order, OrderStatus.CANCELED, false, order.getAllocationError());
        publishMarketplaceStatus(canceledOrder, OrderStatus.CANCELED);
        return toResponse(canceledOrder, "Order canceled");
    }

    public OrderResponse startPreparing(String orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Canceled order cannot be prepared");
        }
        if (order.getStatus() == OrderStatus.PREPARING) {
            return toResponse(order, "Order already preparing");
        }
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before preparing");
        }

        Order preparingOrder = updateOrder(
                order,
                OrderStatus.PREPARING,
                order.isStockAllocated(),
                order.getAllocationError()
        );
        publishMarketplaceStatus(preparingOrder, OrderStatus.PREPARING);
        return toResponse(preparingOrder, "Order preparing");
    }

    public OrderResponse dispatchOrder(String orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Canceled order cannot be dispatched");
        }
        if (order.getStatus() == OrderStatus.DISPATCHED) {
            return toResponse(order, "Order already dispatched");
        }
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order must be preparing before dispatch");
        }

        Order dispatchedOrder = updateOrder(
                order,
                OrderStatus.DISPATCHED,
                order.isStockAllocated(),
                order.getAllocationError()
        );
        publishMarketplaceStatus(dispatchedOrder, OrderStatus.DISPATCHED);
        return toResponse(dispatchedOrder, "Order dispatched");
    }

    public OrderResponse completeOrder(String orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Canceled order cannot be completed");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return toResponse(order, "Order already completed");
        }
        if (order.getStatus() != OrderStatus.DISPATCHED) {
            throw new IllegalStateException("Order must be dispatched before completion");
        }

        Order completedOrder = updateOrder(
                order,
                OrderStatus.COMPLETED,
                order.isStockAllocated(),
                order.getAllocationError()
        );
        publishMarketplaceStatus(completedOrder, OrderStatus.COMPLETED);
        return toResponse(completedOrder, "Order completed");
    }

    private Order updateOrder(Order original, OrderStatus status, boolean stockAllocated, String allocationError) {
        original.setStatus(status);
        original.setStockAllocated(stockAllocated);
        original.setAllocationError(allocationError);
        original.setUpdatedAt(Instant.now());
        return orderRepository.save(original);
    }

    private void registerAuditEvent(String orderId, String eventType, String message) {
        OrderAuditEvent event = OrderAuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .orderId(orderId)
                .eventType(eventType)
                .message(message)
                .createdAt(Instant.now())
                .build();
        orderAuditEventRepository.save(event);
    }

    private List<OrderItem> normalizeItems(List<OrderItemRequest> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("items is required");
        }

        return requestedItems.stream()
                .map(this::toOrderItem)
                .toList();
    }

    private OrderItem toOrderItem(OrderItemRequest requestItem) {
        String productId = normalizeRequired(requestItem.getProductId(), "productId");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown productId: " + productId));

        if (!product.isActive()) {
            throw new IllegalStateException("Product is inactive: " + productId);
        }

        int quantity = requestItem.getQuantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }

        BigDecimal unitPrice = requestItem.getUnitPrice() == null
                ? product.getUnitPrice()
                : requestItem.getUnitPrice();
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be >= 0");
        }

        String nameSnapshot = trimToNull(requestItem.getNameSnapshot());
        if (nameSnapshot == null) {
            nameSnapshot = product.getName();
        }

        return OrderItem.builder()
                .productId(productId)
                .nameSnapshot(nameSnapshot)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizeExternalId(OrderSource source, String externalId) {
        if (source == OrderSource.INTERNAL) {
            return trimToNull(externalId);
        }
        return normalizeRequired(externalId, "externalId");
    }

    private String normalizeMerchantId(OrderSource source, String merchantId) {
        if (source == OrderSource.INTERNAL) {
            return trimToNull(merchantId);
        }
        return normalizeRequired(merchantId, "merchantId");
    }

    private Order findOrder(String orderId) {
        String normalizedOrderId = normalizeRequired(orderId, "id");
        return orderRepository.findById(normalizedOrderId)
                .orElseThrow(() -> new OrderNotFoundException(normalizedOrderId));
    }

    private OrderResponse toResponse(Order order, String message) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .nameSnapshot(item.getNameSnapshot())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .source(order.getSource())
                .merchantId(order.getMerchantId())
                .externalId(order.getExternalId())
                .customerName(order.getCustomerName())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .stockAllocated(order.isStockAllocated())
                .allocationError(order.getAllocationError())
                .message(message)
                .build();
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void publishMarketplaceStatus(Order order, OrderStatus status) {
        if (marketplaceStatusSyncService == null) {
            return;
        }

        try {
            marketplaceStatusSyncService.publish(order.getSource(), order.getMerchantId(), order.getExternalId(), status);
        } catch (Exception ex) {
            log.warn(
                    "Failed to publish marketplace status for order {} (source={} status={}): {}",
                    order.getId(),
                    order.getSource(),
                    status,
                    ex.getMessage()
            );
        }
    }
}
