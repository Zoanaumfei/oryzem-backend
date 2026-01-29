package com.oryzem.backend.domain.aws.file;

public record PresignDownloadResponse(
        String downloadUrl,
        long expiresInSeconds
) {
}
