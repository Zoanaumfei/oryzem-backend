package com.oryzem.backend.modules.items.domain;

import com.oryzem.backend.shared.exceptions.NotFoundException;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(String supplierID, String partNumberVersion) {
        super(String.format(
                "Item %s/%s nao encontrado",
                supplierID,
                partNumberVersion
        ));
    }
}


