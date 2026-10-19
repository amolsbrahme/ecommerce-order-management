package com.tenacious.ecommerce.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String identifier) {
        super("Order not found: " + identifier);
    }
}
