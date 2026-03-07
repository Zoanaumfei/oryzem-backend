package com.oryzem.backend.modules.sync.controller;

import com.oryzem.backend.modules.sync.dto.OrderSyncResponse;
import com.oryzem.backend.modules.sync.service.OrderSyncService;
import com.oryzem.backend.shared.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class OrderSyncController {

    private final OrderSyncService orderSyncService;

    @PostMapping("/orders")
    @Operation(
            summary = "Manually sync new orders from marketplaces",
            description = "Starts an authenticated synchronization cycle against enabled marketplace connectors."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Synchronization completed successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderSyncResponse> syncOrders() {
        return ResponseEntity.ok(orderSyncService.syncOrders());
    }
}
