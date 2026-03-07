package com.oryzem.backend.modules.items.controller;

import com.oryzem.backend.modules.items.domain.ItemStatus;
import com.oryzem.backend.modules.items.dto.ItemRequest;
import com.oryzem.backend.modules.items.dto.ItemResponse;
import com.oryzem.backend.modules.items.service.ItemService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Manage supplier items available to internal and external users")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @Operation(
            summary = "Create item",
            description = "Creates a new supplier item."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Item payload to create"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid item payload",
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
            )
    })
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request) {

        ItemResponse response = itemService.createItem(request);

        log.info("POST /api/v1/items - Sucesso: {}", response);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "List items",
            description = "Returns all registered supplier items."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items returned successfully"),
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
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        List<ItemResponse> response = itemService.getAllItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{supplierID}/{partNumberVersion}")
    @Operation(
            summary = "Get item by supplier and versioned part number",
            description = "Returns a single item identified by supplier ID and versioned part number."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item returned successfully"),
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
                    description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ItemResponse> getItem(
            @Parameter(description = "Supplier identifier", example = "SUP-001")
            @PathVariable String supplierID,
            @Parameter(description = "Part number version", example = "PN-1000-A")
            @PathVariable String partNumberVersion) {

        ItemResponse response =
                itemService.getItem(supplierID, partNumberVersion);

        log.info("GET /api/v1/items/{}/{} - Sucesso",
                supplierID, partNumberVersion);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "List items by status",
            description = "Returns all items currently in the informed status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items returned successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value",
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
            )
    })
    public ResponseEntity<List<ItemResponse>> getItemsByStatus(
            @Parameter(description = "Item lifecycle status", example = "ACTIVE")
            @PathVariable ItemStatus status) {
        List<ItemResponse> response = itemService.getItemsByStatus(status);
        return ResponseEntity.ok(response);
    }
}
