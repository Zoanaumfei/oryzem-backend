package com.oryzem.backend.modules.vehicles.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class VehicleProject {

    private String projectId;
    private String als;
    private String customer;
    private String projectName;
    private String description;
    private String rgTemplate;
    private String status;
    private Integer progress;
    private String me;
    private String pvs;
    private String s0;
    private String sop;
    private String tppa;
    private String vff;
    private String bodyFramePvs;
    private String bodyFrameS0;
    private String bodyFrameVff;
    private String pvsEletTbt;
    private String s0EletTbt;
    private String vffEletTbt;
    private String pvsM100;
    private String s0M100;
    private String vffM100;
    private String pvsZp5Tbt;
    private String s0Zp5Tbt;
    private String vffZp5Tbt;
    private String pvsZp7Tbt;
    private String s0Zp7Tbt;
    private String vffZp7Tbt;
    private String updatedAt;
    private String updatedBy;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    public String getProjectId() {
        return projectId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    public String getAls() {
        return als;
    }

    @DynamoDbAttribute("customer")
    public String getCustomer() {
        return customer;
    }

    @DynamoDbAttribute("projectName")
    public String getProjectName() {
        return projectName;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    @DynamoDbAttribute("rgTemplate")
    public String getRgTemplate() {
        return rgTemplate;
    }

    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }

    @DynamoDbAttribute("progress")
    public Integer getProgress() {
        return progress;
    }

    @DynamoDbAttribute("ME")
    public String getMe() {
        return me;
    }

    @DynamoDbAttribute("PVS")
    public String getPvs() {
        return pvs;
    }

    @DynamoDbAttribute("S0")
    public String getS0() {
        return s0;
    }

    @DynamoDbAttribute("SOP")
    public String getSop() {
        return sop;
    }

    @DynamoDbAttribute("TPPA")
    public String getTppa() {
        return tppa;
    }

    @DynamoDbAttribute("VFF")
    public String getVff() {
        return vff;
    }

    @DynamoDbAttribute("bodyFrame_PVS")
    public String getBodyFramePvs() {
        return bodyFramePvs;
    }

    @DynamoDbAttribute("bodyFrame_S0")
    public String getBodyFrameS0() {
        return bodyFrameS0;
    }

    @DynamoDbAttribute("bodyFrame_VFF")
    public String getBodyFrameVff() {
        return bodyFrameVff;
    }

    @DynamoDbAttribute("PVS_ELET_TBT")
    public String getPvsEletTbt() {
        return pvsEletTbt;
    }

    @DynamoDbAttribute("S0_ELET_TBT")
    public String getS0EletTbt() {
        return s0EletTbt;
    }

    @DynamoDbAttribute("VFF_ELET_TBT")
    public String getVffEletTbt() {
        return vffEletTbt;
    }

    @DynamoDbAttribute("PVS_M100")
    public String getPvsM100() {
        return pvsM100;
    }

    @DynamoDbAttribute("S0_M100")
    public String getS0M100() {
        return s0M100;
    }

    @DynamoDbAttribute("VFF_M100")
    public String getVffM100() {
        return vffM100;
    }

    @DynamoDbAttribute("PVS_ZP5_TBT")
    public String getPvsZp5Tbt() {
        return pvsZp5Tbt;
    }

    @DynamoDbAttribute("S0_ZP5_TBT")
    public String getS0Zp5Tbt() {
        return s0Zp5Tbt;
    }

    @DynamoDbAttribute("VFF_ZP5_TBT")
    public String getVffZp5Tbt() {
        return vffZp5Tbt;
    }

    @DynamoDbAttribute("PVS_ZP7_TBT")
    public String getPvsZp7Tbt() {
        return pvsZp7Tbt;
    }

    @DynamoDbAttribute("S0_ZP7_TBT")
    public String getS0Zp7Tbt() {
        return s0Zp7Tbt;
    }

    @DynamoDbAttribute("VFF_ZP7_TBT")
    public String getVffZp7Tbt() {
        return vffZp7Tbt;
    }

    @DynamoDbAttribute("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @DynamoDbAttribute("updatedBy")
    public String getUpdatedBy() {
        return updatedBy;
    }
}
