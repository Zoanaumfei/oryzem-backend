package com.oryzem.backend.modules.vehicles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oryzem.backend.modules.vehicles.validation.ValidVehicleProject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidVehicleProject
@Schema(description = "Payload used to create or update ALS planning data for a vehicle project")
public class VehicleProjectUpsertRequest {

    private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String DATE_MESSAGE = "must be YYYY-MM-DD";

    @JsonProperty("customer")
    @Schema(description = "Customer name", example = "Volkswagen")
    @NotBlank(message = "customer is required")
    private String customer;

    @JsonProperty("projectName")
    @Schema(description = "Project name", example = "TCross PA2")
    @NotBlank(message = "projectName is required")
    private String projectName;

    @JsonProperty("description")
    @Schema(description = "Project description", example = "Atualizacao de acabamento interno")
    @NotBlank(message = "description is required")
    private String description;

    @JsonProperty("rgTemplate")
    @Schema(description = "Reference RG template", example = "RG-ALS-2026")
    @NotBlank(message = "rgTemplate is required")
    private String rgTemplate;

    @JsonProperty("status")
    @Schema(description = "Current project status", example = "IN_PROGRESS")
    @NotBlank(message = "status is required")
    private String status;

    @JsonProperty("progress")
    @Schema(description = "Completion percentage", example = "65")
    @NotNull(message = "progress is required")
    @Min(value = 0, message = "progress must be between 0 and 100")
    @Max(value = 100, message = "progress must be between 0 and 100")
    private Integer progress;

    @JsonProperty("ME")
    @Schema(description = "ME milestone date", example = "2026-04-01")
    @NotBlank(message = "ME is required")
    @Pattern(regexp = DATE_PATTERN, message = "ME " + DATE_MESSAGE)
    private String me;

    @JsonProperty("PVS")
    @Schema(description = "PVS milestone date", example = "2026-05-01")
    @NotBlank(message = "PVS is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS " + DATE_MESSAGE)
    private String pvs;

    @JsonProperty("S0")
    @Schema(description = "S0 milestone date", example = "2026-06-01")
    @NotBlank(message = "S0 is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0 " + DATE_MESSAGE)
    private String s0;

    @JsonProperty("SOP")
    @Schema(description = "SOP milestone date", example = "2026-09-01")
    @NotBlank(message = "SOP is required")
    @Pattern(regexp = DATE_PATTERN, message = "SOP " + DATE_MESSAGE)
    private String sop;

    @JsonProperty("TPPA")
    @Schema(description = "TPPA milestone date", example = "2026-03-15")
    @NotBlank(message = "TPPA is required")
    @Pattern(regexp = DATE_PATTERN, message = "TPPA " + DATE_MESSAGE)
    private String tppa;

    @JsonProperty("VFF")
    @Schema(description = "VFF milestone date", example = "2026-07-15")
    @NotBlank(message = "VFF is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF " + DATE_MESSAGE)
    private String vff;

    @JsonProperty("bodyFrame_PVS")
    @Schema(description = "Body frame PVS date", example = "2026-05-03")
    @NotBlank(message = "bodyFrame_PVS is required")
    @Pattern(regexp = DATE_PATTERN, message = "bodyFrame_PVS " + DATE_MESSAGE)
    private String bodyFramePvs;

    @JsonProperty("bodyFrame_S0")
    @Schema(description = "Body frame S0 date", example = "2026-06-03")
    @NotBlank(message = "bodyFrame_S0 is required")
    @Pattern(regexp = DATE_PATTERN, message = "bodyFrame_S0 " + DATE_MESSAGE)
    private String bodyFrameS0;

    @JsonProperty("bodyFrame_VFF")
    @Schema(description = "Body frame VFF date", example = "2026-07-18")
    @NotBlank(message = "bodyFrame_VFF is required")
    @Pattern(regexp = DATE_PATTERN, message = "bodyFrame_VFF " + DATE_MESSAGE)
    private String bodyFrameVff;

    @JsonProperty("PVS_ELET_TBT")
    @Schema(description = "PVS electrical TBT date", example = "2026-05-05")
    @NotBlank(message = "PVS_ELET_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_ELET_TBT " + DATE_MESSAGE)
    private String pvsEletTbt;

    @JsonProperty("S0_ELET_TBT")
    @Schema(description = "S0 electrical TBT date", example = "2026-06-05")
    @NotBlank(message = "S0_ELET_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_ELET_TBT " + DATE_MESSAGE)
    private String s0EletTbt;

    @JsonProperty("VFF_ELET_TBT")
    @Schema(description = "VFF electrical TBT date", example = "2026-07-20")
    @NotBlank(message = "VFF_ELET_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_ELET_TBT " + DATE_MESSAGE)
    private String vffEletTbt;

    @JsonProperty("PVS_M100")
    @Schema(description = "PVS M100 date", example = "2026-05-08")
    @NotBlank(message = "PVS_M100 is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_M100 " + DATE_MESSAGE)
    private String pvsM100;

    @JsonProperty("S0_M100")
    @Schema(description = "S0 M100 date", example = "2026-06-08")
    @NotBlank(message = "S0_M100 is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_M100 " + DATE_MESSAGE)
    private String s0M100;

    @JsonProperty("VFF_M100")
    @Schema(description = "VFF M100 date", example = "2026-07-24")
    @NotBlank(message = "VFF_M100 is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_M100 " + DATE_MESSAGE)
    private String vffM100;

    @JsonProperty("PVS_ZP5_TBT")
    @Schema(description = "PVS ZP5 TBT date", example = "2026-05-10")
    @NotBlank(message = "PVS_ZP5_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_ZP5_TBT " + DATE_MESSAGE)
    private String pvsZp5Tbt;

    @JsonProperty("S0_ZP5_TBT")
    @Schema(description = "S0 ZP5 TBT date", example = "2026-06-10")
    @NotBlank(message = "S0_ZP5_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_ZP5_TBT " + DATE_MESSAGE)
    private String s0Zp5Tbt;

    @JsonProperty("VFF_ZP5_TBT")
    @Schema(description = "VFF ZP5 TBT date", example = "2026-07-26")
    @NotBlank(message = "VFF_ZP5_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_ZP5_TBT " + DATE_MESSAGE)
    private String vffZp5Tbt;

    @JsonProperty("PVS_ZP7_TBT")
    @Schema(description = "PVS ZP7 TBT date", example = "2026-05-12")
    @NotBlank(message = "PVS_ZP7_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_ZP7_TBT " + DATE_MESSAGE)
    private String pvsZp7Tbt;

    @JsonProperty("S0_ZP7_TBT")
    @Schema(description = "S0 ZP7 TBT date", example = "2026-06-12")
    @NotBlank(message = "S0_ZP7_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_ZP7_TBT " + DATE_MESSAGE)
    private String s0Zp7Tbt;

    @JsonProperty("VFF_ZP7_TBT")
    @Schema(description = "VFF ZP7 TBT date", example = "2026-07-28")
    @NotBlank(message = "VFF_ZP7_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_ZP7_TBT " + DATE_MESSAGE)
    private String vffZp7Tbt;
}
