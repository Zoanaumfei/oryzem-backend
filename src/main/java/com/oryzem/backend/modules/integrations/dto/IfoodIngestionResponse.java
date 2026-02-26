package com.oryzem.backend.modules.integrations.dto;

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
public class IfoodIngestionResponse {

    private int processedEvents;
    private int importedOrders;
    private int duplicateOrders;
    private int failedEvents;

    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
