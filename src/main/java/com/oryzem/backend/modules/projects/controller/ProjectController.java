package com.oryzem.backend.modules.projects.controller;

import com.oryzem.backend.modules.projects.dto.CreateProjectRequest;
import com.oryzem.backend.modules.projects.dto.ProjectResponse;
import com.oryzem.backend.modules.projects.dto.ProjectSummaryResponse;
import com.oryzem.backend.modules.projects.dto.UpdateProjectRequest;
import com.oryzem.backend.modules.projects.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key is required")
            @Pattern(
                    regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
                    message = "Idempotency-Key must be a valid UUID"
            ) String requestId,
            @Valid @RequestBody CreateProjectRequest request) {

        ProjectResponse response = projectService.createProject(request, requestId);
        log.info("POST /api/v1/projects - Sucesso: {}", response.projectId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key is required")
            @Pattern(
                    regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
                    message = "Idempotency-Key must be a valid UUID"
            ) String requestId,
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        ProjectResponse response = projectService.updateProject(projectId, request, requestId);
        log.info("PUT /api/v1/projects/{} - Sucesso", projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>> listProjects() {
        List<ProjectSummaryResponse> response = projectService.listProjects();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable String projectId) {

        ProjectResponse response = projectService.getProject(projectId);
        return ResponseEntity.ok(response);
    }
}
