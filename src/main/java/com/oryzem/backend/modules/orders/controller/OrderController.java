package com.oryzem.backend.modules.orders.controller;

import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderResponse;
import com.oryzem.backend.modules.orders.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Internal and external order management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create internal or external order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        HttpStatus status = "Order already exists".equals(response.getMessage()) ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by id")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm order and allocate inventory")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order and release inventory if needed")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PostMapping("/{id}/prepare")
    @Operation(summary = "Mark order as preparing")
    public ResponseEntity<OrderResponse> prepareOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.startPreparing(id));
    }

    @PostMapping("/{id}/dispatch")
    @Operation(summary = "Mark order as dispatched")
    public ResponseEntity<OrderResponse> dispatchOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.dispatchOrder(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark order as completed")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }
}
