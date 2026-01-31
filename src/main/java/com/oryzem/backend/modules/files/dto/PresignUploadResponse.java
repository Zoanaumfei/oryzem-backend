package com.oryzem.backend.modules.files.dto;

public record PresignUploadResponse(
        String key,
        String uploadUrl,
        long expiresInSeconds
) {
}

