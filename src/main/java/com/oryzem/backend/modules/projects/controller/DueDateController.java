package com.oryzem.backend.modules.projects.controller;

import com.oryzem.backend.modules.projects.dto.DueDateResponse;
import com.oryzem.backend.modules.projects.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/due")
@RequiredArgsConstructor
public class DueDateController {

    private final ProjectService projectService;

    @GetMapping("/{date}")
    public ResponseEntity<DueDateResponse> getDueDate(
            @PathVariable String date,
            @RequestParam(required = false) String pageToken,
            @RequestParam(defaultValue = "100") int limit) {

        DueDateResponse response = projectService.getDueDate(date, pageToken, limit);
        log.info("GET /api/v1/due/{} - items: {}", date, response.items().size());
        return ResponseEntity.ok(response);
    }
}
