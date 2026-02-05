package com.oryzem.backend.modules.projects.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectResponse(
        @NotBlank String projectId,
        @NotBlank String projectName,
        @NotNull @Valid Grid grid,
        @NotBlank String updatedAt
) {
}
