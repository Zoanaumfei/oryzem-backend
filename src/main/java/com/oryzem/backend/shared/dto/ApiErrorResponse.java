package com.oryzem.backend.shared.dto;

public record ApiErrorResponse(
        String timestamp,
        String path,
        String message
) {
}

