package com.oryzem.backend.domain.aws.monthlybirthday;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBirthdayRequest {

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @NotNull(message = "Year is required")
    @Min(value = 1000, message = "Year must be YYYY")
    @Max(value = 9999, message = "Year must be YYYY")
    private Integer year;

    @NotNull(message = "CorporateMonth is required")
    @Min(value = 1, message = "CorporateMonth must be between 1 and 12")
    @Max(value = 12, message = "CorporateMonth must be between 1 and 12")
    private Integer corporateMonth;

    @NotNull(message = "CorporateYear is required")
    @Min(value = 1000, message = "CorporateYear must be YYYY")
    @Max(value = 9999, message = "CorporateYear must be YYYY")
    private Integer corporateYear;
}
