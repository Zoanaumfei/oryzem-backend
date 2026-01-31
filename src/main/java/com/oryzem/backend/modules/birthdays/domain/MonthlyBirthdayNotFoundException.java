package com.oryzem.backend.modules.birthdays.domain;

import com.oryzem.backend.shared.exceptions.NotFoundException;

public class MonthlyBirthdayNotFoundException extends NotFoundException {
    public MonthlyBirthdayNotFoundException(Integer month, String name) {
        super(String.format("Birthday %s/%s not found", month, name));
    }
}


