package com.oryzem.backend.domain.aws.monthlybirthday;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<MonthlyBirthdayResponse>> getBirthdays(
            @RequestParam(value = "month", required = false) Integer month) {
        if (month == null) {
            return ResponseEntity.ok(service.getAllBirthdays());
        }
        return ResponseEntity.ok(service.getBirthdaysByMonth(month));
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
