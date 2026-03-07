package com.oryzem.backend.modules.inventory.repository;

import com.oryzem.backend.core.tenant.TenantScope;
import com.oryzem.backend.modules.inventory.domain.InventoryMovement;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InventoryMovementRepository {

    private final ConcurrentMap<String, ConcurrentMap<String, InventoryMovement>> movementsByTenantAndId = new ConcurrentHashMap<>();

    public InventoryMovement save(InventoryMovement movement) {
        InventoryMovement copy = copy(movement);
        tenantMovements(TenantScope.current()).put(copy.getId(), copy);
        return copy(copy);
    }

    public List<InventoryMovement> findByReferenceOrderId(String referenceOrderId) {
        List<InventoryMovement> movements = new ArrayList<>();
        for (InventoryMovement movement : tenantMovements(TenantScope.current()).values()) {
            if (referenceOrderId != null && referenceOrderId.equals(movement.getReferenceOrderId())) {
                movements.add(copy(movement));
            }
        }
        return movements;
    }

    private ConcurrentMap<String, InventoryMovement> tenantMovements(String tenantScope) {
        return movementsByTenantAndId.computeIfAbsent(tenantScope, ignored -> new ConcurrentHashMap<>());
    }

    private InventoryMovement copy(InventoryMovement movement) {
        return InventoryMovement.builder()
                .id(movement.getId())
                .productId(movement.getProductId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .reason(movement.getReason())
                .referenceOrderId(movement.getReferenceOrderId())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
