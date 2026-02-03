package com.oryzem.backend.modules.birthdays.service;

import com.oryzem.backend.modules.birthdays.domain.MonthlyBirthday;
import com.oryzem.backend.modules.birthdays.dto.MonthlyBirthdayRequest;
import com.oryzem.backend.modules.birthdays.dto.MonthlyBirthdayResponse;

public class MonthlyBirthdayMapper {

    private MonthlyBirthdayMapper() {
        // prevent instantiation
    }

    public static MonthlyBirthday toDomain(MonthlyBirthdayRequest request) {
        return MonthlyBirthday.builder()
                .month(request.getMonth())
                .day(request.getDay())
                .name(request.getName())
                .corporateMonth(request.getCorporateMonth())
                .corporateYear(request.getCorporateYear())
                .photoKey(request.getPhotoKey())
                .build();
    }

    public static MonthlyBirthdayResponse toResponse(MonthlyBirthday birthday, String message) {
        return MonthlyBirthdayResponse.builder()
                .month(birthday.getMonth())
                .day(birthday.getDay())
                .name(birthday.getName())
                .corporateMonth(birthday.getCorporateMonth())
                .corporateYear(birthday.getCorporateYear())
                .photoKey(birthday.getPhotoKey())
                .message(message)
                .build();
    }
}

