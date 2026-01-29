package com.oryzem.backend.domain.aws.monthlybirthday;

public class MonthlyBirthdayMapper {

    private MonthlyBirthdayMapper() {
        // prevent instantiation
    }

    public static MonthlyBirthday toDomain(MonthlyBirthdayRequest request) {
        return MonthlyBirthday.builder()
                .month(request.getMonth())
                .day(request.getDay())
                .name(request.getName())
                .year(request.getYear())
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
                .year(birthday.getYear())
                .corporateMonth(birthday.getCorporateMonth())
                .corporateYear(birthday.getCorporateYear())
                .photoKey(birthday.getPhotoKey())
                .message(message)
                .build();
    }
}
