package com.oryzem.backend.modules.birthdays.controller;

import com.oryzem.backend.modules.birthdays.dto.MonthlyBirthdayRequest;
import com.oryzem.backend.modules.birthdays.dto.MonthlyBirthdayResponse;
import com.oryzem.backend.modules.birthdays.service.MonthlyBirthdayService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/birthdays")
@RequiredArgsConstructor
@Tag(name = "Birthdays", description = "Manage monthly birthdays shown in the internal portal")
@SecurityRequirement(name = "bearerAuth")
public class MonthlyBirthdayController {

    private final MonthlyBirthdayService service;

    @PostMapping
    @Operation(
            summary = "Create birthday entry",
            description = "Creates a new birthday entry for the monthly birthday board."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Birthday payload to create"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Birthday entry created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid birthday payload",
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
                    description = "Birthday entry already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MonthlyBirthdayResponse> createBirthday(
            @Valid @RequestBody MonthlyBirthdayRequest request) {

        MonthlyBirthdayResponse response = service.createBirthday(request);
        log.info("POST /api/v1/birthdays - Success: {}/{}",
                response.getMonth(),
                response.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping
    @Operation(
            summary = "Update birthday entry",
            description = "Updates an existing birthday entry."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Birthday payload to update"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Birthday entry updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid birthday payload",
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
                    description = "Birthday entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MonthlyBirthdayResponse> updateBirthday(
            @Valid @RequestBody MonthlyBirthdayRequest request) {

        MonthlyBirthdayResponse response = service.updateBirthday(request);
        log.info("PUT /api/v1/birthdays - Success: {}/{}",
                response.getMonth(),
                response.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "List birthday entries",
            description = "Returns birthday entries filtered by month, by name, or by both when both filters are informed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Birthday entries returned successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter values",
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
    public ResponseEntity<List<MonthlyBirthdayResponse>> getBirthdays(
            @Parameter(description = "Month number from 1 to 12", example = "3")
            @RequestParam(value = "month", required = false) Integer month,
            @Parameter(description = "Partial name filter", example = "Ana")
            @RequestParam(value = "name", required = false) String name) {
        if (month != null && name != null && !name.isBlank()) {
            return ResponseEntity.ok(service.getBirthdaysByMonthAndNameContains(month, name));
        }
        if (month != null) {
            return ResponseEntity.ok(service.getBirthdaysByMonth(month));
        }
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(service.getBirthdaysByNameContains(name));
        }
        return ResponseEntity.ok(service.getAllBirthdays());
    }

    @GetMapping("/{month}/{name}")
    @Operation(
            summary = "Get a birthday entry",
            description = "Returns a birthday entry identified by month and name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Birthday entry returned successfully"),
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
                    description = "Birthday entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MonthlyBirthdayResponse> getBirthday(
            @Parameter(description = "Month number from 1 to 12", example = "3")
            @PathVariable Integer month,
            @Parameter(description = "Exact name stored for the birthday entry", example = "Ana Silva")
            @PathVariable String name) {
        MonthlyBirthdayResponse response = service.getBirthday(month, name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{month}/{name}")
    @Operation(
            summary = "Delete a birthday entry",
            description = "Deletes a birthday entry identified by month and name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Birthday entry deleted successfully"),
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
                    description = "Birthday entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteBirthday(
            @Parameter(description = "Month number from 1 to 12", example = "3")
            @PathVariable Integer month,
            @Parameter(description = "Exact name stored for the birthday entry", example = "Ana Silva")
            @PathVariable String name) {
        service.deleteBirthday(month, name);
        return ResponseEntity.noContent().build();
    }
}
