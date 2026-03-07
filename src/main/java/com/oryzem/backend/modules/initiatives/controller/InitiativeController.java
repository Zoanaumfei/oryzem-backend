package com.oryzem.backend.modules.initiatives.controller;

import com.oryzem.backend.modules.initiatives.dto.InitiativeRequest;
import com.oryzem.backend.modules.initiatives.dto.InitiativeResponse;
import com.oryzem.backend.modules.initiatives.service.InitiativeService;
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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/initiatives")
@RequiredArgsConstructor
@Validated
@Tag(name = "Initiatives", description = "Manage annual initiatives tracked by the internal portal")
@SecurityRequirement(name = "bearerAuth")
public class InitiativeController {

    private final InitiativeService initiativeService;

    @GetMapping
    @Operation(
            summary = "List initiatives by year",
            description = "Returns all initiatives registered for the informed year."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Initiatives returned successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Year must be informed in YYYY format",
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
    public ResponseEntity<List<InitiativeResponse>> listInitiativesByYear(
            @Parameter(description = "Reference year in YYYY format", example = "2026")
            @RequestParam("year")
            @Pattern(regexp = "^\\d{4}$", message = "Year must be YYYY") String year
    ) {
        List<InitiativeResponse> response = initiativeService.listInitiativesByYear(year);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
            summary = "Create initiative",
            description = "Creates a new initiative. An optional idempotency key can be sent to avoid duplicated submissions."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Initiative payload to create"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Initiative created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid initiative payload",
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
                    responseCode = "409",
                    description = "Business rule conflict while creating the initiative",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InitiativeResponse> createInitiative(
            @Parameter(description = "Optional idempotency key for safe retries", example = "9f4d5d8a-2df3-49f6-b6e7-3fbc2c0248e5")
            @RequestHeader(value = "Idempotency-Key", required = false) String requestId,
            @Valid @RequestBody InitiativeRequest request
    ) {
        InitiativeResponse response = initiativeService.createInitiative(request, requestId);
        log.info("POST /api/v1/initiatives - Success: {}", response.getInitiativeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @Operation(
            summary = "Update initiative",
            description = "Updates an existing initiative identified by the payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Initiative payload to update"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Initiative updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid initiative payload",
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
                    description = "Initiative not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InitiativeResponse> updateInitiative(
            @Valid @RequestBody InitiativeRequest request
    ) {
        InitiativeResponse response = initiativeService.updateInitiative(request);
        log.info("PUT /api/v1/initiatives - Success: {}", response.getInitiativeId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{initiativeId}")
    @Operation(
            summary = "Delete initiative",
            description = "Deletes an initiative by identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Initiative deleted successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid initiative identifier",
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
                    description = "Initiative not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InitiativeResponse> deleteInitiative(
            @Parameter(description = "Initiative identifier", example = "INIT-2026-001")
            @PathVariable
            @NotBlank(message = "InitiativeId is required")
            @Size(max = 100, message = "InitiativeId must be at most 100 characters")
            String initiativeId
    ) {
        InitiativeResponse response = initiativeService.deleteInitiative(initiativeId);
        log.info("DELETE /api/v1/initiatives/{} - Success", initiativeId);
        return ResponseEntity.ok(response);
    }
}
