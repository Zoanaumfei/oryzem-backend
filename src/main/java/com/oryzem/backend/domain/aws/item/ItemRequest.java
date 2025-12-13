package com.oryzem.backend.domain.aws.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    @NotBlank(message = "PartNumberID é obrigatório")
    @Size(max = 50, message = "PartNumberID deve ter no máximo 50 caracteres")
    private String partNumberID;

    @NotBlank(message = "SupplierID é obrigatório")
    @Size(max = 50, message = "SupplierID deve ter no máximo 50 caracteres")
    private String supplierID;
}

