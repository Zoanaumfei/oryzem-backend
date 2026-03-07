package com.oryzem.backend.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiErrorResponse(
        @Schema(description = "Timestamp in ISO-8601 format when the error was generated", example = "2026-03-06T21:12:45Z")
        String timestamp,
        @Schema(description = "Request path that triggered the error", example = "/api/orders/ORD-123")
        String path,
        @Schema(description = "Human-readable error message", example = "Order not found")
        String message
) {
}

