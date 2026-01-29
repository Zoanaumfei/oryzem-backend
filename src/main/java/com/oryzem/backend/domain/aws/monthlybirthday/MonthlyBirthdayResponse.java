package com.oryzem.backend.domain.aws.monthlybirthday;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyBirthdayResponse {
    private Integer month;
    private String name;
    private Integer year;
    private Integer corporateMonth;
    private Integer corporateYear;
    private String message;
}
