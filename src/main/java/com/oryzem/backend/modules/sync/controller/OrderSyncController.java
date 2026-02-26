package com.oryzem.backend.modules.sync.controller;

import com.oryzem.backend.modules.sync.dto.OrderSyncResponse;
import com.oryzem.backend.modules.sync.service.OrderSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integrations/sync")
@RequiredArgsConstructor
@Tag(name = "Integrations", description = "Marketplace synchronization endpoints")
public class OrderSyncController {

    private final OrderSyncService orderSyncService;

    @PostMapping("/orders")
    @Operation(summary = "Manually sync new orders from marketplaces")
    public ResponseEntity<OrderSyncResponse> syncOrders() {
        return ResponseEntity.ok(orderSyncService.syncOrders());
    }
}
