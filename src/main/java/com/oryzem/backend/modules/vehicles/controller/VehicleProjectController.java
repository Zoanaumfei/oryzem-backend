package com.oryzem.backend.modules.vehicles.controller;

import com.oryzem.backend.modules.vehicles.dto.VehicleProjectResponse;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectUpsertRequest;
import com.oryzem.backend.modules.vehicles.service.VehicleProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/als")
@RequiredArgsConstructor
@Validated
public class VehicleProjectController {

    private final VehicleProjectService service;

    @PutMapping("/{als}")
    public ResponseEntity<VehicleProjectResponse> upsert(
            @PathVariable @NotBlank String projectId,
            @PathVariable @NotBlank String als,
            @Valid @RequestBody VehicleProjectUpsertRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        VehicleProjectResponse response = service.upsert(projectId, als, request, jwt);
        log.info("PUT /api/v1/projects/{}/als/{} - Success", projectId, als);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VehicleProjectResponse>> listByProjectId(
            @PathVariable @NotBlank String projectId) {

        List<VehicleProjectResponse> response = service.listByProjectId(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{als}")
    public ResponseEntity<VehicleProjectResponse> getOne(
            @PathVariable @NotBlank String projectId,
            @PathVariable @NotBlank String als) {

        VehicleProjectResponse response = service.getById(projectId, als);
        return ResponseEntity.ok(response);
    }
}
