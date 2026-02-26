package com.oryzem.backend.modules.orders.domain;

import com.oryzem.backend.shared.exceptions.NotFoundException;

public class OrderNotFoundException extends NotFoundException {

    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
