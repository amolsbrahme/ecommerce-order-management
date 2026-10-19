package com.tenacious.ecommerce.order.model;

public enum OrderStatus {
    /** Order row created, stock reservation in progress. */
    PENDING,
    /** All items reserved successfully; order-placed event published. */
    CONFIRMED,
    /** Stock reservation failed (insufficient stock or Inventory Service unavailable). */
    FAILED,
    CANCELLED
}
