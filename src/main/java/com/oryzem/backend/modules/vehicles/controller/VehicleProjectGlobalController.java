package com.oryzem.backend.modules.vehicles.controller;

import com.oryzem.backend.modules.vehicles.dto.VehicleProjectSummaryResponse;
import com.oryzem.backend.modules.vehicles.service.VehicleProjectService;
import com.oryzem.backend.shared.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Vehicle Projects", description = "Global views for ALS records linked to vehicle projects")
@SecurityRequirement(name = "bearerAuth")
public class VehicleProjectGlobalController {

    private final VehicleProjectService service;

    @GetMapping
    @Operation(
            summary = "List ALS entries across all projects",
            description = "Returns a summary list of all ALS records regardless of project."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ALS records returned successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<VehicleProjectSummaryResponse>> listAll() {
        List<VehicleProjectSummaryResponse> response = service.listAll();
        log.info("GET /api/v1/projects/als - Success");
        return ResponseEntity.ok(response);
    }
}
