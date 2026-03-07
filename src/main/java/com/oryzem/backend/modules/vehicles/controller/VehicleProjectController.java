package com.oryzem.backend.modules.vehicles.controller;

import com.oryzem.backend.modules.vehicles.dto.VehicleProjectResponse;
import com.oryzem.backend.modules.vehicles.dto.VehicleProjectUpsertRequest;
import com.oryzem.backend.modules.vehicles.service.VehicleProjectService;
import com.oryzem.backend.shared.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Vehicle Projects", description = "Manage ALS records scoped to a vehicle project")
@SecurityRequirement(name = "bearerAuth")
public class VehicleProjectController {

    private final VehicleProjectService service;

    @PutMapping("/{als}")
    @Operation(
            summary = "Create or update a project ALS entry",
            description = "Creates a new ALS entry for the project or updates the existing one."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "ALS payload for the project"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ALS entry created or updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project, ALS, or payload data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
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
    public ResponseEntity<VehicleProjectResponse> upsert(
            @Parameter(description = "Project identifier", example = "PRJ-001")
            @PathVariable @NotBlank String projectId,
            @Parameter(description = "ALS identifier", example = "ALS-001")
            @PathVariable @NotBlank String als,
            @Valid @RequestBody VehicleProjectUpsertRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt) {

        VehicleProjectResponse response = service.upsert(projectId, als, request, jwt);
        log.info("PUT /api/v1/projects/{}/als/{} - Success", projectId, als);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "List ALS entries by project",
            description = "Returns all ALS entries linked to the informed project."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ALS entries returned successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project identifier",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<VehicleProjectResponse>> listByProjectId(
            @Parameter(description = "Project identifier", example = "PRJ-001")
            @PathVariable @NotBlank String projectId) {

        List<VehicleProjectResponse> response = service.listByProjectId(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{als}")
    @Operation(
            summary = "Get an ALS entry by project and identifier",
            description = "Returns a single ALS entry linked to the informed project."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ALS entry returned successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project or ALS identifier",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "ALS entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<VehicleProjectResponse> getOne(
            @Parameter(description = "Project identifier", example = "PRJ-001")
            @PathVariable @NotBlank String projectId,
            @Parameter(description = "ALS identifier", example = "ALS-001")
            @PathVariable @NotBlank String als) {

        VehicleProjectResponse response = service.getById(projectId, als);
        return ResponseEntity.ok(response);
    }
}
