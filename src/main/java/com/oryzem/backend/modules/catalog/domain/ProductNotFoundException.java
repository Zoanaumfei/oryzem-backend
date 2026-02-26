package com.oryzem.backend.modules.catalog.domain;

import com.oryzem.backend.shared.exceptions.NotFoundException;

public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}
