package com.oryzem.backend.modules.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request for generating a pre-signed S3 upload URL")
public record PresignUploadRequest(
        @Schema(description = "Original file name", example = "report.pdf")
        @NotBlank String originalFileName,
        @Schema(description = "MIME type of the file", example = "application/pdf")
        @NotBlank String contentType,
        @Schema(description = "File size in bytes", example = "1048576")
        @NotNull @Positive Long sizeBytes
) {
}
