package com.oryzem.backend.domain.aws.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponse {
    private String partNumberID;
    private String supplierID;
    private String createdAt;
    private String message;
}
