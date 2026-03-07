package com.oryzem.backend.modules.vehicles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ALS planning data returned for a vehicle project")
public class VehicleProjectResponse {

    @JsonProperty("projectId")
    @Schema(description = "Project identifier", example = "VW247_1")
    private String projectId;

    @JsonProperty("als")
    @Schema(description = "ALS identifier", example = "ALS1")
    private String als;

    @JsonProperty("customer")
    @Schema(description = "Customer name", example = "Volkswagen")
    private String customer;

    @JsonProperty("projectName")
    @Schema(description = "Project name", example = "TCross PA2")
    private String projectName;

    @JsonProperty("description")
    @Schema(description = "Project description", example = "Atualizacao de acabamento interno")
    private String description;

    @JsonProperty("rgTemplate")
    @Schema(description = "Reference RG template", example = "RG-ALS-2026")
    private String rgTemplate;

    @JsonProperty("status")
    @Schema(description = "Current project status", example = "IN_PROGRESS")
    private String status;

    @JsonProperty("progress")
    @Schema(description = "Completion percentage", example = "65")
    private Integer progress;

    @JsonProperty("ME")
    @Schema(description = "ME milestone date", example = "2026-04-01")
    private String me;

    @JsonProperty("PVS")
    @Schema(description = "PVS milestone date", example = "2026-05-01")
    private String pvs;

    @JsonProperty("S0")
    @Schema(description = "S0 milestone date", example = "2026-06-01")
    private String s0;

    @JsonProperty("SOP")
    @Schema(description = "SOP milestone date", example = "2026-09-01")
    private String sop;

    @JsonProperty("TPPA")
    @Schema(description = "TPPA milestone date", example = "2026-03-15")
    private String tppa;

    @JsonProperty("VFF")
    @Schema(description = "VFF milestone date", example = "2026-07-15")
    private String vff;

    @JsonProperty("bodyFrame_PVS")
    @Schema(description = "Body frame PVS date", example = "2026-05-03")
    private String bodyFramePvs;

    @JsonProperty("bodyFrame_S0")
    @Schema(description = "Body frame S0 date", example = "2026-06-03")
    private String bodyFrameS0;

    @JsonProperty("bodyFrame_VFF")
    @Schema(description = "Body frame VFF date", example = "2026-07-18")
    private String bodyFrameVff;

    @JsonProperty("PVS_ELET_TBT")
    @Schema(description = "PVS electrical TBT date", example = "2026-05-05")
    private String pvsEletTbt;

    @JsonProperty("S0_ELET_TBT")
    @Schema(description = "S0 electrical TBT date", example = "2026-06-05")
    private String s0EletTbt;

    @JsonProperty("VFF_ELET_TBT")
    @Schema(description = "VFF electrical TBT date", example = "2026-07-20")
    private String vffEletTbt;

    @JsonProperty("PVS_M100")
    @Schema(description = "PVS M100 date", example = "2026-05-08")
    private String pvsM100;

    @JsonProperty("S0_M100")
    @Schema(description = "S0 M100 date", example = "2026-06-08")
    private String s0M100;

    @JsonProperty("VFF_M100")
    @Schema(description = "VFF M100 date", example = "2026-07-24")
    private String vffM100;

    @JsonProperty("PVS_ZP5_TBT")
    @Schema(description = "PVS ZP5 TBT date", example = "2026-05-10")
    private String pvsZp5Tbt;

    @JsonProperty("S0_ZP5_TBT")
    @Schema(description = "S0 ZP5 TBT date", example = "2026-06-10")
    private String s0Zp5Tbt;

    @JsonProperty("VFF_ZP5_TBT")
    @Schema(description = "VFF ZP5 TBT date", example = "2026-07-26")
    private String vffZp5Tbt;

    @JsonProperty("PVS_ZP7_TBT")
    @Schema(description = "PVS ZP7 TBT date", example = "2026-05-12")
    private String pvsZp7Tbt;

    @JsonProperty("S0_ZP7_TBT")
    @Schema(description = "S0 ZP7 TBT date", example = "2026-06-12")
    private String s0Zp7Tbt;

    @JsonProperty("VFF_ZP7_TBT")
    @Schema(description = "VFF ZP7 TBT date", example = "2026-07-28")
    private String vffZp7Tbt;

    @JsonProperty("updatedAt")
    @Schema(description = "Last update timestamp", example = "2026-03-06T22:30:00Z")
    private String updatedAt;

    @JsonProperty("updatedBy")
    @Schema(description = "User that last updated the record", example = "maria.silva@oryzem.com")
    private String updatedBy;
}
