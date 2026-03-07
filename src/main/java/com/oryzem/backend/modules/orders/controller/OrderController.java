package com.oryzem.backend.modules.orders.controller;

import com.oryzem.backend.modules.orders.domain.OrderNotFoundException;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.service.CanonicalOrderCommandService;
import com.oryzem.backend.modules.orders.service.CanonicalOrderQueryService;
import com.oryzem.backend.modules.orders.service.OrderService;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Internal and external order management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final CanonicalOrderQueryService canonicalOrderQueryService;
    private final CanonicalOrderCommandService canonicalOrderCommandService;

    @PostMapping
    @Operation(
            summary = "Create internal or external order",
            description = "Creates a new order. If the source order already exists, the existing record is returned with status 200."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Order payload with customer, store, and item data"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "200", description = "Order already existed and was returned"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
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
                    description = "Business rule conflict while creating the order",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        HttpStatus status = "Order already exists".equals(response.getMessage()) ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List orders from the canonical Postgres model",
            description = "Returns a paginated list of orders filtered by status, store, and date range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders returned successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter or pagination values",
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
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @Parameter(description = "Filter by order status", example = "CONFIRMED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by store identifier", example = "store-001")
            @RequestParam(required = false) String storeId,
            @Parameter(description = "Start date in ISO format", example = "2026-03-01")
            @RequestParam(required = false) LocalDate from,
            @Parameter(description = "End date in ISO format", example = "2026-03-31")
            @RequestParam(required = false) LocalDate to,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(canonicalOrderQueryService.listOrders(status, storeId, from, to, page, size));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get order by id",
            description = "Returns a single order by identifier, using the canonical model with fallback to the legacy service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned successfully"),
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
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order identifier", example = "ORD-20260306-001")
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(canonicalOrderQueryService.getOrderById(id));
        } catch (OrderNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.ok(orderService.getOrderById(id));
        }
    }

    @PostMapping("/{id}/confirm")
    @Operation(
            summary = "Confirm order and allocate inventory",
            description = "Moves the order to the confirmed state and reserves stock when required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order confirmed successfully"),
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
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid order state transition",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> confirmOrder(
            @Parameter(description = "Order identifier", example = "ORD-20260306-001")
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(canonicalOrderCommandService.confirmOrder(id));
        } catch (OrderNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.ok(orderService.confirmOrder(id));
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel order and release inventory if needed",
            description = "Cancels the order and returns reserved inventory when applicable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order canceled successfully"),
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
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid order state transition",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order identifier", example = "ORD-20260306-001")
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(canonicalOrderCommandService.cancelOrder(id));
        } catch (OrderNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.ok(orderService.cancelOrder(id));
        }
    }

    @PostMapping("/{id}/prepare")
    @Operation(
            summary = "Mark order as preparing",
            description = "Transitions the order to the preparing state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order marked as preparing"),
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
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid order state transition",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> prepareOrder(
            @Parameter(description = "Order identifier", example = "ORD-20260306-001")
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(canonicalOrderCommandService.startPreparing(id));
        } catch (OrderNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.ok(orderService.startPreparing(id));
        }
    }

    @PostMapping("/{id}/dispatch")
    @Operation(
            summary = "Mark order as dispatched",
            description = "Transitions the order to the dispatched state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order marked as dispatched"),
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
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid order state transition",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> dispatchOrder(
            @Parameter(description = "Order identifier", example = "ORD-20260306-001")
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(canonicalOrderCommandService.dispatchOrder(id));
        } catch (OrderNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.ok(orderService.dispatchOrder(id));
        }
    }

    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Mark order as completed",
            description = "Transitions the order to the completed state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order marked as completed"),
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
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid order state transition",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "Order identifier", example = "ORD-20260306-001")
            @PathVariable String id) {
        try {
            return ResponseEntity.ok(canonicalOrderCommandService.completeOrder(id));
        } catch (OrderNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.ok(orderService.completeOrder(id));
        }
    }
}
