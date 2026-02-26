package com.oryzem.backend.modules.inventory.service;

import com.oryzem.backend.modules.catalog.repository.ProductRepository;
import com.oryzem.backend.modules.inventory.domain.InsufficientStockException;
import com.oryzem.backend.modules.inventory.domain.InventoryItem;
import com.oryzem.backend.modules.inventory.domain.InventoryMovement;
import com.oryzem.backend.modules.inventory.domain.InventoryMovementType;
import com.oryzem.backend.modules.inventory.dto.InventoryItemResponse;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementRequest;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementResponse;
import com.oryzem.backend.modules.inventory.repository.InventoryItemRepository;
import com.oryzem.backend.modules.inventory.repository.InventoryMovementRepository;
import com.oryzem.backend.modules.orders.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final String REASON_ORDER_CONFIRMED = "ORDER_CONFIRMED";
    private static final String REASON_ORDER_CANCELED_RESTOCK = "ORDER_CANCELED_RESTOCK";

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public synchronized InventoryMovementResponse applyMovement(InventoryMovementRequest request) {
        String productId = normalizeRequired(request.getProductId(), "productId");
        ensureProductExists(productId);

        InventoryItem current = inventoryItemRepository.findByProductId(productId)
                .orElseGet(() -> emptyInventory(productId));

        int minimumLevel = request.getMinimumLevel() == null
                ? current.getMinimumLevel()
                : Math.max(request.getMinimumLevel(), 0);

        int quantityAvailable = applyDelta(current.getQuantityAvailable(), request.getQuantity(), request.getType(), productId);
        InventoryItem updatedItem = InventoryItem.builder()
                .productId(productId)
                .quantityAvailable(quantityAvailable)
                .minimumLevel(minimumLevel)
                .updatedAt(Instant.now())
                .build();
        inventoryItemRepository.save(updatedItem);

        InventoryMovement movement = InventoryMovement.builder()
                .id(UUID.randomUUID().toString())
                .productId(productId)
                .type(request.getType())
                .quantity(request.getQuantity())
                .reason(normalizeRequired(request.getReason(), "reason"))
                .referenceOrderId(trimToNull(request.getReferenceOrderId()))
                .createdAt(Instant.now())
                .build();
        InventoryMovement savedMovement = inventoryMovementRepository.save(movement);
        return toMovementResponse(savedMovement);
    }

    public synchronized InventoryItemResponse getInventoryByProductId(String productId) {
        String normalizedProductId = normalizeRequired(productId, "productId");
        ensureProductExists(normalizedProductId);
        InventoryItem item = inventoryItemRepository.findByProductId(normalizedProductId)
                .orElseGet(() -> emptyInventory(normalizedProductId));
        return toInventoryResponse(item);
    }

    public synchronized void reserveStockForOrder(String orderId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Map<String, Integer> quantitiesByProduct = items.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        for (Map.Entry<String, Integer> entry : quantitiesByProduct.entrySet()) {
            String productId = normalizeRequired(entry.getKey(), "productId");
            ensureProductExists(productId);

            InventoryItem item = inventoryItemRepository.findByProductId(productId)
                    .orElseGet(() -> emptyInventory(productId));
            if (item.getQuantityAvailable() < entry.getValue()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product " + productId
                                + ": available=" + item.getQuantityAvailable()
                                + ", requested=" + entry.getValue()
                );
            }
        }

        for (OrderItem item : items) {
            createOrderMovement(orderId, item.getProductId(), item.getQuantity(), InventoryMovementType.OUT, REASON_ORDER_CONFIRMED);
        }
    }

    public synchronized void releaseStockForOrder(String orderId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (OrderItem item : items) {
            createOrderMovement(orderId, item.getProductId(), item.getQuantity(), InventoryMovementType.IN, REASON_ORDER_CANCELED_RESTOCK);
        }
    }

    public List<InventoryMovement> getMovementsByOrderId(String orderId) {
        return inventoryMovementRepository.findByReferenceOrderId(orderId);
    }

    private void createOrderMovement(
            String orderId,
            String productId,
            int quantity,
            InventoryMovementType type,
            String reason
    ) {
        InventoryMovementRequest movementRequest = InventoryMovementRequest.builder()
                .productId(productId)
                .type(type)
                .quantity(quantity)
                .reason(reason)
                .referenceOrderId(orderId)
                .build();
        applyMovement(movementRequest);
    }

    private int applyDelta(int currentQuantity, int quantity, InventoryMovementType type, String productId) {
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }

        return switch (type) {
            case IN, ADJUSTMENT -> currentQuantity + quantity;
            case OUT -> {
                int updated = currentQuantity - quantity;
                if (updated < 0) {
                    throw new InsufficientStockException("Insufficient stock for product " + productId);
                }
                yield updated;
            }
        };
    }

    private void ensureProductExists(String productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown productId: " + productId));
    }

    private InventoryItem emptyInventory(String productId) {
        return InventoryItem.builder()
                .productId(productId)
                .quantityAvailable(0)
                .minimumLevel(0)
                .updatedAt(Instant.now())
                .build();
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovement movement) {
        return InventoryMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProductId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .reason(movement.getReason())
                .referenceOrderId(movement.getReferenceOrderId())
                .createdAt(movement.getCreatedAt())
                .build();
    }

    private InventoryItemResponse toInventoryResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .productId(item.getProductId())
                .quantityAvailable(item.getQuantityAvailable())
                .minimumLevel(item.getMinimumLevel())
                .updatedAt(item.getUpdatedAt())
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
}
