package com.oryzem.backend.modules.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Result of a marketplace order synchronization cycle")
public class OrderSyncResponse {

    @Schema(description = "Number of imported orders", example = "5")
    private int importedCount;

    @Schema(description = "Number of duplicate orders skipped", example = "1")
    private int duplicateCount;

    @Schema(description = "Number of failed imports", example = "0")
    private int failedCount;

    @Schema(description = "Identifiers of imported or affected orders")
    @Builder.Default
    private List<String> orderIds = new ArrayList<>();

    @Schema(description = "Errors found during synchronization")
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
