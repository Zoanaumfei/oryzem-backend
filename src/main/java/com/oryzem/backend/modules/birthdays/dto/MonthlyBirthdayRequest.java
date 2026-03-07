package com.oryzem.backend.modules.birthdays.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Payload used to create or update a monthly birthday entry")
public class MonthlyBirthdayRequest {

    @JsonProperty("month")
    @Schema(description = "Birth month number", example = "3")
    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @JsonProperty("day")
    @Schema(description = "Birth day number", example = "14")
    @NotNull(message = "Day is required")
    @Min(value = 1, message = "Day must be between 1 and 31")
    @Max(value = 31, message = "Day must be between 1 and 31")
    private Integer day;

    @JsonProperty("name")
    @Schema(description = "Collaborator name", example = "Ana Silva")
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @JsonProperty("corporate_month")
    @Schema(description = "Corporate anniversary month number", example = "7")
    @NotNull(message = "CorporateMonth is required")
    @Min(value = 1, message = "CorporateMonth must be between 1 and 12")
    @Max(value = 12, message = "CorporateMonth must be between 1 and 12")
    private Integer corporateMonth;

    @JsonProperty("corporate_year")
    @Schema(description = "Corporate anniversary year", example = "2021")
    @NotNull(message = "CorporateYear is required")
    @Min(value = 1000, message = "CorporateYear must be YYYY")
    @Max(value = 9999, message = "CorporateYear must be YYYY")
    private Integer corporateYear;

    @JsonProperty("photo_key")
    @Schema(description = "Optional S3 key for the collaborator photo", example = "birthdays/ana-silva.jpg")
    private String photoKey;
}
