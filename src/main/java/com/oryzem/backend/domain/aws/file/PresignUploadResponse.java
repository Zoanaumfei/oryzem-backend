package com.oryzem.backend.domain.aws.file;

public record PresignUploadResponse(
        String key,
        String uploadUrl,
        long expiresInSeconds
) {
}
