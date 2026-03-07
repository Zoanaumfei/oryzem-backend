package com.oryzem.backend.modules.inventory.repository;

import com.oryzem.backend.core.tenant.TenantScope;
import com.oryzem.backend.modules.inventory.domain.InventoryItem;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InventoryItemRepository {

    private final ConcurrentMap<String, ConcurrentMap<String, InventoryItem>> itemsByTenantAndProductId = new ConcurrentHashMap<>();

    public InventoryItem save(InventoryItem item) {
        InventoryItem copy = copy(item);
        tenantItems(TenantScope.current()).put(copy.getProductId(), copy);
        return copy(copy);
    }

    public Optional<InventoryItem> findByProductId(String productId) {
        InventoryItem found = tenantItems(TenantScope.current()).get(productId);
        return Optional.ofNullable(found).map(this::copy);
    }

    private ConcurrentMap<String, InventoryItem> tenantItems(String tenantScope) {
        return itemsByTenantAndProductId.computeIfAbsent(tenantScope, ignored -> new ConcurrentHashMap<>());
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
