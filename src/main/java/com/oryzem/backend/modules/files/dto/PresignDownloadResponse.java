package com.oryzem.backend.modules.files.dto;

public record PresignDownloadResponse(
        String downloadUrl,
        long expiresInSeconds
) {
}

