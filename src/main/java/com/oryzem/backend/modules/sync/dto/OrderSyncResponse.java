package com.oryzem.backend.modules.sync.dto;

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
public class OrderSyncResponse {

    private int importedCount;
    private int duplicateCount;
    private int failedCount;

    @Builder.Default
    private List<String> orderIds = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
