package com.socommerce.app.entity;

/**
 * Where a paid order has reached after payment, in the order it normally moves through.
 * A paid order with no value yet is treated as CONFIRMED.
 */
public enum FulfillmentStatus {
    CONFIRMED("Order confirmed"),
    PACKED("Packed"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNED("Returned");

    private final String label;

    FulfillmentStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
