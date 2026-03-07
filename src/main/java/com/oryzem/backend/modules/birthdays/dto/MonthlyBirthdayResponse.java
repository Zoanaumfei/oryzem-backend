package com.oryzem.backend.modules.birthdays.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Birthday entry returned by the API")
public class MonthlyBirthdayResponse {
    @JsonProperty("month")
    @Schema(description = "Birth month number", example = "3")
    private Integer month;
    @JsonProperty("day")
    @Schema(description = "Birth day number", example = "14")
    private Integer day;
    @JsonProperty("name")
    @Schema(description = "Collaborator name", example = "Ana Silva")
    private String name;
    @JsonProperty("corporate_month")
    @Schema(description = "Corporate anniversary month number", example = "7")
    private Integer corporateMonth;
    @JsonProperty("corporate_year")
    @Schema(description = "Corporate anniversary year", example = "2021")
    private Integer corporateYear;
    @JsonProperty("photo_key")
    @Schema(description = "Optional S3 key for the collaborator photo", example = "birthdays/ana-silva.jpg")
    private String photoKey;
    @Schema(description = "Additional outcome message", example = "Birthday created successfully")
    private String message;
}
