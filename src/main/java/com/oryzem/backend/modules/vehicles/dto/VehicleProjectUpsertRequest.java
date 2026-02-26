package com.oryzem.backend.modules.vehicles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oryzem.backend.modules.vehicles.validation.ValidVehicleProject;
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
public class VehicleProjectUpsertRequest {

    private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String DATE_MESSAGE = "must be YYYY-MM-DD";

    @JsonProperty("customer")
    @NotBlank(message = "customer is required")
    private String customer;

    @JsonProperty("projectName")
    @NotBlank(message = "projectName is required")
    private String projectName;

    @JsonProperty("description")
    @NotBlank(message = "description is required")
    private String description;

    @JsonProperty("rgTemplate")
    @NotBlank(message = "rgTemplate is required")
    private String rgTemplate;

    @JsonProperty("status")
    @NotBlank(message = "status is required")
    private String status;

    @JsonProperty("progress")
    @NotNull(message = "progress is required")
    @Min(value = 0, message = "progress must be between 0 and 100")
    @Max(value = 100, message = "progress must be between 0 and 100")
    private Integer progress;

    @JsonProperty("ME")
    @NotBlank(message = "ME is required")
    @Pattern(regexp = DATE_PATTERN, message = "ME " + DATE_MESSAGE)
    private String me;

    @JsonProperty("PVS")
    @NotBlank(message = "PVS is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS " + DATE_MESSAGE)
    private String pvs;

    @JsonProperty("S0")
    @NotBlank(message = "S0 is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0 " + DATE_MESSAGE)
    private String s0;

    @JsonProperty("SOP")
    @NotBlank(message = "SOP is required")
    @Pattern(regexp = DATE_PATTERN, message = "SOP " + DATE_MESSAGE)
    private String sop;

    @JsonProperty("TPPA")
    @NotBlank(message = "TPPA is required")
    @Pattern(regexp = DATE_PATTERN, message = "TPPA " + DATE_MESSAGE)
    private String tppa;

    @JsonProperty("VFF")
    @NotBlank(message = "VFF is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF " + DATE_MESSAGE)
    private String vff;

    @JsonProperty("bodyFrame_PVS")
    @NotBlank(message = "bodyFrame_PVS is required")
    @Pattern(regexp = DATE_PATTERN, message = "bodyFrame_PVS " + DATE_MESSAGE)
    private String bodyFramePvs;

    @JsonProperty("bodyFrame_S0")
    @NotBlank(message = "bodyFrame_S0 is required")
    @Pattern(regexp = DATE_PATTERN, message = "bodyFrame_S0 " + DATE_MESSAGE)
    private String bodyFrameS0;

    @JsonProperty("bodyFrame_VFF")
    @NotBlank(message = "bodyFrame_VFF is required")
    @Pattern(regexp = DATE_PATTERN, message = "bodyFrame_VFF " + DATE_MESSAGE)
    private String bodyFrameVff;

    @JsonProperty("PVS_ELET_TBT")
    @NotBlank(message = "PVS_ELET_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_ELET_TBT " + DATE_MESSAGE)
    private String pvsEletTbt;

    @JsonProperty("S0_ELET_TBT")
    @NotBlank(message = "S0_ELET_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_ELET_TBT " + DATE_MESSAGE)
    private String s0EletTbt;

    @JsonProperty("VFF_ELET_TBT")
    @NotBlank(message = "VFF_ELET_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_ELET_TBT " + DATE_MESSAGE)
    private String vffEletTbt;

    @JsonProperty("PVS_M100")
    @NotBlank(message = "PVS_M100 is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_M100 " + DATE_MESSAGE)
    private String pvsM100;

    @JsonProperty("S0_M100")
    @NotBlank(message = "S0_M100 is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_M100 " + DATE_MESSAGE)
    private String s0M100;

    @JsonProperty("VFF_M100")
    @NotBlank(message = "VFF_M100 is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_M100 " + DATE_MESSAGE)
    private String vffM100;

    @JsonProperty("PVS_ZP5_TBT")
    @NotBlank(message = "PVS_ZP5_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_ZP5_TBT " + DATE_MESSAGE)
    private String pvsZp5Tbt;

    @JsonProperty("S0_ZP5_TBT")
    @NotBlank(message = "S0_ZP5_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_ZP5_TBT " + DATE_MESSAGE)
    private String s0Zp5Tbt;

    @JsonProperty("VFF_ZP5_TBT")
    @NotBlank(message = "VFF_ZP5_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_ZP5_TBT " + DATE_MESSAGE)
    private String vffZp5Tbt;

    @JsonProperty("PVS_ZP7_TBT")
    @NotBlank(message = "PVS_ZP7_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "PVS_ZP7_TBT " + DATE_MESSAGE)
    private String pvsZp7Tbt;

    @JsonProperty("S0_ZP7_TBT")
    @NotBlank(message = "S0_ZP7_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "S0_ZP7_TBT " + DATE_MESSAGE)
    private String s0Zp7Tbt;

    @JsonProperty("VFF_ZP7_TBT")
    @NotBlank(message = "VFF_ZP7_TBT is required")
    @Pattern(regexp = DATE_PATTERN, message = "VFF_ZP7_TBT " + DATE_MESSAGE)
    private String vffZp7Tbt;
}
