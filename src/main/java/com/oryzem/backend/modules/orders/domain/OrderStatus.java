package com.oryzem.backend.modules.orders.domain;

public enum OrderStatus {
    RECEIVED,
    CONFIRMED,
    PREPARING,
    DISPATCHED,
    COMPLETED,
    CANCELED,
    ALLOCATION_ERROR
}
