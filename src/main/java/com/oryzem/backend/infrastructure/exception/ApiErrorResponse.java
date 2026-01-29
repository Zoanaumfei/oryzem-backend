package com.oryzem.backend.infrastructure.exception;

public record ApiErrorResponse(
        String timestamp,
        String path,
        String message
) {
}
