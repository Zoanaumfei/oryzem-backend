package com.oryzem.backend.modules.projects.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record DueDateResponse(
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String date,
        @NotNull List<@Valid DueDateItem> items,
        String nextPageToken
) {
}
