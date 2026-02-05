package com.oryzem.backend.modules.projects.dto;

import com.oryzem.backend.modules.projects.domain.Gate;
import com.oryzem.backend.modules.projects.domain.Phase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DueDateItem(
        @NotBlank String projectId,
        @NotBlank String projectName,
        @Min(1) @Max(8) int als,
        @NotNull Gate gate,
        @NotNull Phase phase,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String dueDate
) {
}
