package com.oryzem.backend.modules.inventory.domain;

public class InsufficientStockException extends IllegalStateException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
