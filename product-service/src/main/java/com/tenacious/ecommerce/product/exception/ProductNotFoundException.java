package com.tenacious.ecommerce.product.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String id) {
        super("Product not found with id: " + id);
    }

    public static ProductNotFoundException forSku(String sku) {
        return new ProductNotFoundException("(lookup by sku) " + sku);
    }
}
