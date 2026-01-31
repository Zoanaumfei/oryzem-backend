package com.oryzem.backend.modules.birthdays.controller;

import com.oryzem.backend.modules.birthdays.dto.MonthlyBirthdayRequest;
import com.oryzem.backend.modules.birthdays.dto.MonthlyBirthdayResponse;
import com.oryzem.backend.modules.birthdays.service.MonthlyBirthdayService;
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
public class MonthlyBirthdayController {

    private final MonthlyBirthdayService service;

    @PostMapping
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
    public ResponseEntity<MonthlyBirthdayResponse> updateBirthday(
            @Valid @RequestBody MonthlyBirthdayRequest request) {

        MonthlyBirthdayResponse response = service.updateBirthday(request);
        log.info("PUT /api/v1/birthdays - Success: {}/{}",
                response.getMonth(),
                response.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MonthlyBirthdayResponse>> getBirthdays(
            @RequestParam(value = "month", required = false) Integer month,
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
    public ResponseEntity<MonthlyBirthdayResponse> getBirthday(
            @PathVariable Integer month,
            @PathVariable String name) {
        MonthlyBirthdayResponse response = service.getBirthday(month, name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{month}/{name}")
    public ResponseEntity<Void> deleteBirthday(
            @PathVariable Integer month,
            @PathVariable String name) {
        service.deleteBirthday(month, name);
        return ResponseEntity.noContent().build();
    }
}

