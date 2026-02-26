package com.oryzem.backend.modules.vehicles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleProjectResponse {

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("als")
    private String als;

    @JsonProperty("customer")
    private String customer;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("rgTemplate")
    private String rgTemplate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("progress")
    private Integer progress;

    @JsonProperty("ME")
    private String me;

    @JsonProperty("PVS")
    private String pvs;

    @JsonProperty("S0")
    private String s0;

    @JsonProperty("SOP")
    private String sop;

    @JsonProperty("TPPA")
    private String tppa;

    @JsonProperty("VFF")
    private String vff;

    @JsonProperty("bodyFrame_PVS")
    private String bodyFramePvs;

    @JsonProperty("bodyFrame_S0")
    private String bodyFrameS0;

    @JsonProperty("bodyFrame_VFF")
    private String bodyFrameVff;

    @JsonProperty("PVS_ELET_TBT")
    private String pvsEletTbt;

    @JsonProperty("S0_ELET_TBT")
    private String s0EletTbt;

    @JsonProperty("VFF_ELET_TBT")
    private String vffEletTbt;

    @JsonProperty("PVS_M100")
    private String pvsM100;

    @JsonProperty("S0_M100")
    private String s0M100;

    @JsonProperty("VFF_M100")
    private String vffM100;

    @JsonProperty("PVS_ZP5_TBT")
    private String pvsZp5Tbt;

    @JsonProperty("S0_ZP5_TBT")
    private String s0Zp5Tbt;

    @JsonProperty("VFF_ZP5_TBT")
    private String vffZp5Tbt;

    @JsonProperty("PVS_ZP7_TBT")
    private String pvsZp7Tbt;

    @JsonProperty("S0_ZP7_TBT")
    private String s0Zp7Tbt;

    @JsonProperty("VFF_ZP7_TBT")
    private String vffZp7Tbt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("updatedBy")
    private String updatedBy;
}
