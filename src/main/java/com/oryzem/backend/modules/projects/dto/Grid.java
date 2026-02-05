package com.oryzem.backend.modules.projects.dto;

import com.oryzem.backend.modules.projects.domain.Gate;
import com.oryzem.backend.modules.projects.domain.Phase;
import com.oryzem.backend.modules.projects.validation.ValidGrid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@ValidGrid
public record Grid(
        @NotNull
        Map<Integer, Map<Gate, Map<Phase, String>>> dates
) {
}
