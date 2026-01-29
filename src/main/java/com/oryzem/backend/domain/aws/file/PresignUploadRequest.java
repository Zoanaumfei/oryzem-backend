package com.oryzem.backend.domain.aws.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PresignUploadRequest(
        @NotBlank String originalFileName,
        @NotBlank String contentType,
        @NotNull @Positive Long sizeBytes
) {
}
