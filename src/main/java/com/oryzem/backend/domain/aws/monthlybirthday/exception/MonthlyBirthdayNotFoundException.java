package com.oryzem.backend.domain.aws.monthlybirthday.exception;

public class MonthlyBirthdayNotFoundException extends RuntimeException {
    public MonthlyBirthdayNotFoundException(Integer month, String name) {
        super(String.format("Birthday %s/%s not found", month, name));
    }
}
