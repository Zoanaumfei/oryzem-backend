package com.oryzem.backend.domain.aws.item.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String supplierID, String partNumberVersion) {
        super(String.format(
                "Item %s/%s nao encontrado",
                supplierID,
                partNumberVersion
        ));
    }
}
