package com.oryzem.backend.modules.inventory.repository;

import com.oryzem.backend.modules.inventory.domain.InventoryItem;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InventoryItemRepository {

    private final ConcurrentMap<String, InventoryItem> itemsByProductId = new ConcurrentHashMap<>();

    public InventoryItem save(InventoryItem item) {
        InventoryItem copy = copy(item);
        itemsByProductId.put(copy.getProductId(), copy);
        return copy(copy);
    }

    public Optional<InventoryItem> findByProductId(String productId) {
        InventoryItem found = itemsByProductId.get(productId);
        return Optional.ofNullable(found).map(this::copy);
    }

    private InventoryItem copy(InventoryItem item) {
        return InventoryItem.builder()
                .productId(item.getProductId())
                .quantityAvailable(item.getQuantityAvailable())
                .minimumLevel(item.getMinimumLevel())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
