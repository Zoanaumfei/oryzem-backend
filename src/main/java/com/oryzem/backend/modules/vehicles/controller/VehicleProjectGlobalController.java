package com.oryzem.backend.modules.vehicles.controller;

import com.oryzem.backend.modules.vehicles.dto.VehicleProjectSummaryResponse;
import com.oryzem.backend.modules.vehicles.service.VehicleProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/als")
@RequiredArgsConstructor
@Validated
public class VehicleProjectGlobalController {

    private final VehicleProjectService service;

    @GetMapping
    public ResponseEntity<List<VehicleProjectSummaryResponse>> listAll() {
        List<VehicleProjectSummaryResponse> response = service.listAll();
        log.info("GET /api/v1/projects/als - Success");
        return ResponseEntity.ok(response);
    }
}
