package com.oryzem.backend.domain.aws.monthlybirthday;

public class MonthlyBirthdayMapper {

    private MonthlyBirthdayMapper() {
        // prevent instantiation
    }

    public static MonthlyBirthday toDomain(MonthlyBirthdayRequest request) {
        return MonthlyBirthday.builder()
                .month(request.getMonth())
                .name(request.getName())
                .year(request.getYear())
                .corporateMonth(request.getCorporateMonth())
                .corporateYear(request.getCorporateYear())
                .build();
    }

    public static MonthlyBirthdayResponse toResponse(MonthlyBirthday birthday, String message) {
        return MonthlyBirthdayResponse.builder()
                .month(birthday.getMonth())
                .name(birthday.getName())
                .year(birthday.getYear())
                .corporateMonth(birthday.getCorporateMonth())
                .corporateYear(birthday.getCorporateYear())
                .message(message)
                .build();
    }
}
