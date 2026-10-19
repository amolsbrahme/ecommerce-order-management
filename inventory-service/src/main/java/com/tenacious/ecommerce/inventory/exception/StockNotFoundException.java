package com.tenacious.ecommerce.inventory.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String sku) {
        super("No stock record found for sku: " + sku);
    }
}
