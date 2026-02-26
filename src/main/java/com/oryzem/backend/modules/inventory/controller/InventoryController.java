package com.oryzem.backend.modules.inventory.controller;

import com.oryzem.backend.modules.inventory.dto.InventoryItemResponse;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementRequest;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementResponse;
import com.oryzem.backend.modules.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/movements")
    @Operation(summary = "Register inventory movement")
    public ResponseEntity<InventoryMovementResponse> createMovement(
            @Valid @RequestBody InventoryMovementRequest request
    ) {
        InventoryMovementResponse response = inventoryService.applyMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory position for a product")
    public ResponseEntity<InventoryItemResponse> getByProductId(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }
}
