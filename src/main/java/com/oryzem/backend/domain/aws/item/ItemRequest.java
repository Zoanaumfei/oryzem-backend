package com.oryzem.backend.domain.aws.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    @NotBlank(message = "PartNumber is required")
    @Size(max = 50, message = "PartNumber must be at most 50 characters")
    private String partNumber;

    @NotBlank(message = "SupplierID is required")
    @Size(max = 50, message = "SupplierID must be at most 50 characters")
    private String supplierID;

    @NotBlank(message = "ProcessNumber is required")
    @Size(max = 100, message = "ProcessNumber must be at most 100 characters")
    private String processNumber;

    @NotBlank(message = "PartDescription is required")
    @Size(max = 500, message = "PartDescription must be at most 500 characters")
    private String partDescription;

    @NotBlank(message = "TbtVffDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "TbtVffDate must be YYYY/MM/DD"
    )
    private String tbtVffDate;

    @NotBlank(message = "TbtPvsDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "TbtPvsDate must be YYYY/MM/DD"
    )
    private String tbtPvsDate;

    @NotBlank(message = "Tbt0sDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "Tbt0sDate must be YYYY/MM/DD"
    )
    private String tbt0sDate;

    @NotBlank(message = "SopDate is required")
    @Pattern(
            regexp = "^\\d{4}/\\d{2}/\\d{2}$",
            message = "SopDate must be YYYY/MM/DD"
    )
    private String sopDate;
}
