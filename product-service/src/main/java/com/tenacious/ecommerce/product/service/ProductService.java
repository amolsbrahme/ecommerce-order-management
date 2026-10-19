package com.tenacious.ecommerce.product.service;

import com.tenacious.ecommerce.product.dto.ProductRequest;
import com.tenacious.ecommerce.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse getById(String id);

    ProductResponse getBySku(String sku);

    Page<ProductResponse> getAll(Pageable pageable);

    ProductResponse update(String id, ProductRequest request);

    void delete(String id);
}
