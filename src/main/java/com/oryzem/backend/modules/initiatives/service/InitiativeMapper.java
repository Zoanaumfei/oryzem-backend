package com.oryzem.backend.modules.initiatives.service;

import com.oryzem.backend.modules.initiatives.domain.Initiative;
import com.oryzem.backend.modules.initiatives.dto.InitiativeResponse;

public class InitiativeMapper {

    private InitiativeMapper() {
    }

    public static InitiativeResponse toResponse(Initiative initiative, String message) {
        return InitiativeResponse.builder()
                .initiativeId(initiative.getInitiativeId())
                .initiativeName(initiative.getInitiativeName())
                .initiativeNameLower(initiative.getInitiativeNameLower())
                .initiativeCode(initiative.getInitiativeCode())
                .initiativeDescription(initiative.getInitiativeDescription())
                .initiativeType(initiative.getInitiativeType())
                .initiativeDueDate(initiative.getInitiativeDueDate())
                .initiativeStatus(initiative.getInitiativeStatus())
                .leaderName(initiative.getLeaderName())
                .updatedAt(initiative.getUpdatedAt())
                .createdAt(initiative.getCreatedAt())
                .message(message)
                .build();
    }
}
