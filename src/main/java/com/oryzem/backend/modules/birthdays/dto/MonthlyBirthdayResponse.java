package com.oryzem.backend.modules.birthdays.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyBirthdayResponse {
    @JsonProperty("month")
    private Integer month;
    @JsonProperty("day")
    private Integer day;
    @JsonProperty("name")
    private String name;
    @JsonProperty("corporate_month")
    private Integer corporateMonth;
    @JsonProperty("corporate_year")
    private Integer corporateYear;
    @JsonProperty("photo_key")
    private String photoKey;
    private String message;
}

