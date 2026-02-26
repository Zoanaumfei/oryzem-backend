package com.oryzem.backend.modules.vehicles.service;

import com.oryzem.backend.modules.vehicles.domain.VehicleProject;
import com.oryzem.backend.modules.vehicles.domain.VehicleProjectNotFoundException;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectResponse;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectUpsertRequest;
import com.oryzem.backend.modules.vehicles.repository.VehicleProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleProjectService {

    private final VehicleProjectRepository repository;

    public VehicleProjectResponse upsert(String projectId,
                                         String als,
                                         VehicleProjectUpsertRequest request,
                                         Jwt jwt) {
        String normalizedProjectId = normalizeRequiredText(projectId, "projectId");
        String normalizedAls = normalizeRequiredText(als, "als");

        VehicleProject vehicleProject =
                VehicleProjectMapper.toDomain(normalizedProjectId, normalizedAls, request);

        normalizeBusinessFields(vehicleProject);
        vehicleProject.setUpdatedAt(Instant.now().toString());
        vehicleProject.setUpdatedBy(resolveUpdatedBy(jwt));

        VehicleProject saved = repository.save(vehicleProject);
        return VehicleProjectMapper.toResponse(saved);
    }

    public List<VehicleProjectResponse> listByProjectId(String projectId) {
        String normalizedProjectId = normalizeRequiredText(projectId, "projectId");
        return repository.findByProjectId(normalizedProjectId).stream()
                .map(VehicleProjectMapper::toResponse)
                .toList();
    }

    public VehicleProjectResponse getById(String projectId, String als) {
        String normalizedProjectId = normalizeRequiredText(projectId, "projectId");
        String normalizedAls = normalizeRequiredText(als, "als");

        VehicleProject vehicleProject = repository
                .findById(normalizedProjectId, normalizedAls)
                .orElseThrow(() -> new VehicleProjectNotFoundException(normalizedProjectId, normalizedAls));

        return VehicleProjectMapper.toResponse(vehicleProject);
    }

    private void normalizeBusinessFields(VehicleProject vehicleProject) {
        vehicleProject.setCustomer(normalizeRequiredText(vehicleProject.getCustomer(), "customer"));
        vehicleProject.setProjectName(normalizeRequiredText(vehicleProject.getProjectName(), "projectName"));
        vehicleProject.setDescription(normalizeRequiredText(vehicleProject.getDescription(), "description"));
        vehicleProject.setRgTemplate(normalizeRequiredText(vehicleProject.getRgTemplate(), "rgTemplate"));
        vehicleProject.setStatus(normalizeRequiredText(vehicleProject.getStatus(), "status"));
        vehicleProject.setProgress(normalizeProgress(vehicleProject.getProgress()));

        vehicleProject.setMe(normalizeRequiredText(vehicleProject.getMe(), "ME"));
        vehicleProject.setPvs(normalizeRequiredText(vehicleProject.getPvs(), "PVS"));
        vehicleProject.setS0(normalizeRequiredText(vehicleProject.getS0(), "S0"));
        vehicleProject.setSop(normalizeRequiredText(vehicleProject.getSop(), "SOP"));
        vehicleProject.setTppa(normalizeRequiredText(vehicleProject.getTppa(), "TPPA"));
        vehicleProject.setVff(normalizeRequiredText(vehicleProject.getVff(), "VFF"));

        vehicleProject.setBodyFramePvs(normalizeRequiredText(vehicleProject.getBodyFramePvs(), "bodyFrame_PVS"));
        vehicleProject.setBodyFrameS0(normalizeRequiredText(vehicleProject.getBodyFrameS0(), "bodyFrame_S0"));
        vehicleProject.setBodyFrameVff(normalizeRequiredText(vehicleProject.getBodyFrameVff(), "bodyFrame_VFF"));

        vehicleProject.setPvsEletTbt(normalizeRequiredText(vehicleProject.getPvsEletTbt(), "PVS_ELET_TBT"));
        vehicleProject.setS0EletTbt(normalizeRequiredText(vehicleProject.getS0EletTbt(), "S0_ELET_TBT"));
        vehicleProject.setVffEletTbt(normalizeRequiredText(vehicleProject.getVffEletTbt(), "VFF_ELET_TBT"));

        vehicleProject.setPvsM100(normalizeRequiredText(vehicleProject.getPvsM100(), "PVS_M100"));
        vehicleProject.setS0M100(normalizeRequiredText(vehicleProject.getS0M100(), "S0_M100"));
        vehicleProject.setVffM100(normalizeRequiredText(vehicleProject.getVffM100(), "VFF_M100"));

        vehicleProject.setPvsZp5Tbt(normalizeRequiredText(vehicleProject.getPvsZp5Tbt(), "PVS_ZP5_TBT"));
        vehicleProject.setS0Zp5Tbt(normalizeRequiredText(vehicleProject.getS0Zp5Tbt(), "S0_ZP5_TBT"));
        vehicleProject.setVffZp5Tbt(normalizeRequiredText(vehicleProject.getVffZp5Tbt(), "VFF_ZP5_TBT"));

        vehicleProject.setPvsZp7Tbt(normalizeRequiredText(vehicleProject.getPvsZp7Tbt(), "PVS_ZP7_TBT"));
        vehicleProject.setS0Zp7Tbt(normalizeRequiredText(vehicleProject.getS0Zp7Tbt(), "S0_ZP7_TBT"));
        vehicleProject.setVffZp7Tbt(normalizeRequiredText(vehicleProject.getVffZp7Tbt(), "VFF_ZP7_TBT"));
    }

    private Integer normalizeProgress(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("progress is required");
        }
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("progress must be between 0 and 100");
        }
        return value;
    }

    private String resolveUpdatedBy(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Authenticated user email is required");
        }

        String email = trimToNull(jwt.getClaimAsString("email"));
        if (email != null) {
            return "email:" + email;
        }
        throw new IllegalArgumentException("Authenticated user email is required");
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
