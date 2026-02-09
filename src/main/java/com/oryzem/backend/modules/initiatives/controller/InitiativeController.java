package com.oryzem.backend.modules.initiatives.controller;

import com.oryzem.backend.modules.initiatives.dto.InitiativeRequest;
import com.oryzem.backend.modules.initiatives.dto.InitiativeResponse;
import com.oryzem.backend.modules.initiatives.service.InitiativeService;
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
public class InitiativeController {

    private final InitiativeService initiativeService;

    @GetMapping
    public ResponseEntity<List<InitiativeResponse>> listInitiativesByYear(
            @RequestParam("year")
            @Pattern(regexp = "^\\d{4}$", message = "Year must be YYYY") String year
    ) {
        List<InitiativeResponse> response = initiativeService.listInitiativesByYear(year);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<InitiativeResponse> createInitiative(
            @RequestHeader(value = "Idempotency-Key", required = false) String requestId,
            @Valid @RequestBody InitiativeRequest request
    ) {
        InitiativeResponse response = initiativeService.createInitiative(request, requestId);
        log.info("POST /api/v1/initiatives - Success: {}", response.getInitiativeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<InitiativeResponse> updateInitiative(
            @Valid @RequestBody InitiativeRequest request
    ) {
        InitiativeResponse response = initiativeService.updateInitiative(request);
        log.info("PUT /api/v1/initiatives - Success: {}", response.getInitiativeId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{initiativeId}")
    public ResponseEntity<InitiativeResponse> deleteInitiative(
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
