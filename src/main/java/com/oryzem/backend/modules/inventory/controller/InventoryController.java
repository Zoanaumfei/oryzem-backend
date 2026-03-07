package com.oryzem.backend.modules.inventory.controller;

import com.oryzem.backend.modules.inventory.dto.InventoryItemResponse;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementRequest;
import com.oryzem.backend.modules.inventory.dto.InventoryMovementResponse;
import com.oryzem.backend.modules.inventory.service.InventoryService;
import com.oryzem.backend.shared.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/movements")
    @Operation(
            summary = "Register inventory movement",
            description = "Applies an inbound or outbound stock movement for a product."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Inventory movement payload"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movement registered successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid movement payload",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Business rule conflict while applying the movement",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InventoryMovementResponse> createMovement(
            @Valid @RequestBody InventoryMovementRequest request
    ) {
        InventoryMovementResponse response = inventoryService.applyMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    @Operation(
            summary = "Get inventory position for a product",
            description = "Returns the current stock position and movement summary for a product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory position returned successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product or inventory position not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InventoryItemResponse> getByProductId(
            @Parameter(description = "Product identifier", example = "SKU-001")
            @PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }
}
