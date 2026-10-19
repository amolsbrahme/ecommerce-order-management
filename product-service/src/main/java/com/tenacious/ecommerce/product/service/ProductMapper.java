package com.tenacious.ecommerce.product.service;

import com.tenacious.ecommerce.product.dto.ProductRequest;
import com.tenacious.ecommerce.product.dto.ProductResponse;
import com.tenacious.ecommerce.product.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .initialStockHint(request.initialStockHint() == null ? 0 : request.initialStockHint())
                .build();
    }

    public void applyUpdate(Product existing, ProductRequest request) {
        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setCategory(request.category());
        existing.setPrice(request.price());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
