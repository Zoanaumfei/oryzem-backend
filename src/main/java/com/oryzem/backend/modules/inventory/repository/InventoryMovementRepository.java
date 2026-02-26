package com.oryzem.backend.modules.inventory.repository;

import com.oryzem.backend.modules.inventory.domain.InventoryMovement;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InventoryMovementRepository {

    private final ConcurrentMap<String, InventoryMovement> movementsById = new ConcurrentHashMap<>();

    public InventoryMovement save(InventoryMovement movement) {
        InventoryMovement copy = copy(movement);
        movementsById.put(copy.getId(), copy);
        return copy(copy);
    }

    public List<InventoryMovement> findByReferenceOrderId(String referenceOrderId) {
        List<InventoryMovement> movements = new ArrayList<>();
        for (InventoryMovement movement : movementsById.values()) {
            if (referenceOrderId != null && referenceOrderId.equals(movement.getReferenceOrderId())) {
                movements.add(copy(movement));
            }
        }
        return movements;
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
