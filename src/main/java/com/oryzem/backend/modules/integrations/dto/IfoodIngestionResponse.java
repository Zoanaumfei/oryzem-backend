package com.oryzem.backend.modules.integrations.dto;

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
@Schema(description = "Summary of iFood ingestion processing")
public class IfoodIngestionResponse {

    @Schema(description = "Number of events processed", example = "3")
    private int processedEvents;

    @Schema(description = "Number of imported orders", example = "2")
    private int importedOrders;

    @Schema(description = "Number of duplicate orders skipped", example = "1")
    private int duplicateOrders;

    @Schema(description = "Number of failed events", example = "0")
    private int failedEvents;

    @Schema(description = "Processing errors when present")
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
