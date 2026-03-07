package com.oryzem.backend.modules.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing a temporary URL for file download")
public record PresignDownloadResponse(
        @Schema(description = "Temporary URL used to download the file", example = "https://bucket.s3.amazonaws.com/...signature...")
        String downloadUrl,
        @Schema(description = "URL expiration time in seconds", example = "900")
        long expiresInSeconds
) {
}
