package com.oryzem.backend.modules.initiatives.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiativeRequest {

    @NotBlank(message = "InitiativeId is required")
    @Size(max = 100, message = "InitiativeId must be at most 100 characters")
    private String initiativeId;

    @NotBlank(message = "InitiativeName is required")
    @Size(max = 200, message = "InitiativeName must be at most 200 characters")
    private String initiativeName;

    @NotBlank(message = "InitiativeDescription is required")
    @Size(max = 2000, message = "InitiativeDescription must be at most 2000 characters")
    private String initiativeDescription;

    @NotBlank(message = "InitiativeType is required")
    @Size(max = 100, message = "InitiativeType must be at most 100 characters")
    private String initiativeType;

    @NotBlank(message = "InitiativeDueDate is required")
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "InitiativeDueDate must be YYYY-MM-DD"
    )
    private String initiativeDueDate;

    @NotBlank(message = "InitiativeStatus is required")
    @Pattern(
            regexp = "^(IN_PROGRESS|CONCLUDED)$",
            message = "InitiativeStatus must be IN_PROGRESS or CONCLUDED"
    )
    private String initiativeStatus;

    @Size(max = 200, message = "LeaderName must be at most 200 characters")
    private String leaderName;
}
