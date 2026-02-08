package com.oryzem.backend.modules.projects.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record DueDateRangeResponse(
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String startDate,
        @Min(1) @Max(60) int days,
        @NotNull List<@Valid DueDateResponse> dates
) {
}
