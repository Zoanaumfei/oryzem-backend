package com.oryzem.backend.modules.vehicles.service;

import com.oryzem.backend.modules.vehicles.domain.VehicleProject;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectResponse;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectUpsertRequest;

public final class VehicleProjectMapper {

    private VehicleProjectMapper() {
    }

    public static VehicleProject toDomain(String projectId, String als, VehicleProjectUpsertRequest request) {
        return VehicleProject.builder()
                .projectId(projectId)
                .als(als)
                .customer(request.getCustomer())
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .rgTemplate(request.getRgTemplate())
                .status(request.getStatus())
                .progress(request.getProgress())
                .me(request.getMe())
                .pvs(request.getPvs())
                .s0(request.getS0())
                .sop(request.getSop())
                .tppa(request.getTppa())
                .vff(request.getVff())
                .bodyFramePvs(request.getBodyFramePvs())
                .bodyFrameS0(request.getBodyFrameS0())
                .bodyFrameVff(request.getBodyFrameVff())
                .pvsEletTbt(request.getPvsEletTbt())
                .s0EletTbt(request.getS0EletTbt())
                .vffEletTbt(request.getVffEletTbt())
                .pvsM100(request.getPvsM100())
                .s0M100(request.getS0M100())
                .vffM100(request.getVffM100())
                .pvsZp5Tbt(request.getPvsZp5Tbt())
                .s0Zp5Tbt(request.getS0Zp5Tbt())
                .vffZp5Tbt(request.getVffZp5Tbt())
                .pvsZp7Tbt(request.getPvsZp7Tbt())
                .s0Zp7Tbt(request.getS0Zp7Tbt())
                .vffZp7Tbt(request.getVffZp7Tbt())
                .build();
    }

    public static VehicleProjectResponse toResponse(VehicleProject domain) {
        return VehicleProjectResponse.builder()
                .projectId(domain.getProjectId())
                .als(domain.getAls())
                .customer(domain.getCustomer())
                .projectName(domain.getProjectName())
                .description(domain.getDescription())
                .rgTemplate(domain.getRgTemplate())
                .status(domain.getStatus())
                .progress(domain.getProgress())
                .me(domain.getMe())
                .pvs(domain.getPvs())
                .s0(domain.getS0())
                .sop(domain.getSop())
                .tppa(domain.getTppa())
                .vff(domain.getVff())
                .bodyFramePvs(domain.getBodyFramePvs())
                .bodyFrameS0(domain.getBodyFrameS0())
                .bodyFrameVff(domain.getBodyFrameVff())
                .pvsEletTbt(domain.getPvsEletTbt())
                .s0EletTbt(domain.getS0EletTbt())
                .vffEletTbt(domain.getVffEletTbt())
                .pvsM100(domain.getPvsM100())
                .s0M100(domain.getS0M100())
                .vffM100(domain.getVffM100())
                .pvsZp5Tbt(domain.getPvsZp5Tbt())
                .s0Zp5Tbt(domain.getS0Zp5Tbt())
                .vffZp5Tbt(domain.getVffZp5Tbt())
                .pvsZp7Tbt(domain.getPvsZp7Tbt())
                .s0Zp7Tbt(domain.getS0Zp7Tbt())
                .vffZp7Tbt(domain.getVffZp7Tbt())
                .updatedAt(domain.getUpdatedAt())
                .updatedBy(domain.getUpdatedBy())
                .build();
    }
}
