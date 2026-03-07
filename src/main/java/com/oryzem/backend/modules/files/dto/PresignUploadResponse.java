package com.oryzem.backend.modules.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing a temporary URL for file upload")
public record PresignUploadResponse(
        @Schema(description = "Generated object key in S3", example = "initiatives/2026/2f6f6d9e-report.pdf")
        String key,
        @Schema(description = "Temporary URL used to upload the file", example = "https://bucket.s3.amazonaws.com/...signature...")
        String uploadUrl,
        @Schema(description = "URL expiration time in seconds", example = "900")
        long expiresInSeconds
) {
}
