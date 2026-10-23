package com.socommerce.app.entity;

/**
 * The life of an order, in the sequence it normally moves through. CANCELLED and RETURNED
 * are end states that can be reached from several points, which OrderService enforces.
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PLACED,
    CONFIRMED,
    PACKED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED
}
