package com.tenacious.ecommerce.inventory.exception;

public class DuplicateStockException extends RuntimeException {
    public DuplicateStockException(String sku) {
        super("A stock record for sku '" + sku + "' already exists");
    }
}
