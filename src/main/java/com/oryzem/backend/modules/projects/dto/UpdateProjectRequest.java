package com.oryzem.backend.modules.projects.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectRequest(
        @NotNull @Valid Grid grid
) {
}
