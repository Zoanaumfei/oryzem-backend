package com.oryzem.backend.modules.initiatives.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiativeResponse {

    private String initiativeId;
    private String initiativeName;
    private String initiativeNameLower;
    private String initiativeDescription;
    private String initiativeType;
    private String initiativeDueDate;
    private String initiativeStatus;
    private String leaderName;
    private String updatedAt;
    private String createdAt;
    private String message;
}
