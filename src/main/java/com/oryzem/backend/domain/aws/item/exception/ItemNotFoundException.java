package com.oryzem.backend.domain.aws.item.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }


    public ItemNotFoundException(String partNumberID, String supplierID) {
        super(String.format(
                "Item %s/%s não encontrado",
                partNumberID,
                supplierID
        ));
    }
}
