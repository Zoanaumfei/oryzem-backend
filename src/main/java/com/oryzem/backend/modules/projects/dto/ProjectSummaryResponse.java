package com.oryzem.backend.modules.projects.dto;

import com.oryzem.backend.modules.projects.domain.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectSummaryResponse(
        @NotBlank String projectId,
        @NotBlank String projectName,
        @NotNull ProjectStatus status
) {
}
