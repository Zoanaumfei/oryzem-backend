package com.oryzem.backend.modules.orders.repository;

import com.oryzem.backend.modules.orders.domain.Order;
import com.oryzem.backend.modules.orders.domain.OrderItem;
import com.oryzem.backend.modules.orders.domain.OrderSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class OrderRepository {

    private final ConcurrentMap<String, Order> ordersById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> orderIdByExternalKey = new ConcurrentHashMap<>();

    public synchronized Order save(Order order) {
        Order copy = copy(order);
        ordersById.put(copy.getId(), copy);

        if (copy.getSource() != OrderSource.INTERNAL
                && copy.getMerchantId() != null
                && !copy.getMerchantId().isBlank()
                && copy.getExternalId() != null
                && !copy.getExternalId().isBlank()) {
            orderIdByExternalKey.put(
                    buildExternalKey(copy.getSource(), copy.getMerchantId(), copy.getExternalId()),
                    copy.getId()
            );
        }
        return copy(copy);
    }

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(ordersById.get(orderId)).map(this::copy);
    }

    public Optional<Order> findByExternalId(OrderSource source, String merchantId, String externalId) {
        if (source == null || merchantId == null || merchantId.isBlank() || externalId == null || externalId.isBlank()) {
            return Optional.empty();
        }

        String orderId = orderIdByExternalKey.get(buildExternalKey(source, merchantId, externalId));
        if (orderId == null) {
            return Optional.empty();
        }
        return findById(orderId);
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        for (Order order : ordersById.values()) {
            orders.add(copy(order));
        }
        return orders;
    }

    private String buildExternalKey(OrderSource source, String merchantId, String externalId) {
        return source.name() + "::" + merchantId.trim() + "::" + externalId.trim();
    }

    private Order copy(Order order) {
        List<OrderItem> copiedItems = order.getItems().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .nameSnapshot(item.getNameSnapshot())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        return Order.builder()
                .id(order.getId())
                .source(order.getSource())
                .merchantId(order.getMerchantId())
                .externalId(order.getExternalId())
                .customerName(order.getCustomerName())
                .items(copiedItems)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .stockAllocated(order.isStockAllocated())
                .allocationError(order.getAllocationError())
                .build();
    }
}
