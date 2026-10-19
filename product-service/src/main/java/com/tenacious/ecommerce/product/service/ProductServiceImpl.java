package com.tenacious.ecommerce.product.service;

import com.tenacious.ecommerce.product.dto.ProductRequest;
import com.tenacious.ecommerce.product.dto.ProductResponse;
import com.tenacious.ecommerce.product.exception.DuplicateSkuException;
import com.tenacious.ecommerce.product.exception.ProductNotFoundException;
import com.tenacious.ecommerce.product.model.Product;
import com.tenacious.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }
        Product saved = productRepository.save(productMapper.toEntity(request));
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(String id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        return productMapper.toResponse(
                productRepository.findBySku(sku).orElseThrow(() -> ProductNotFoundException.forSku(sku)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    @Override
    public ProductResponse update(String id, ProductRequest request) {
        Product existing = findOrThrow(id);
        productMapper.applyUpdate(existing, request);
        return productMapper.toResponse(productRepository.save(existing));
    }

    @Override
    public void delete(String id) {
        Product existing = findOrThrow(id);
        productRepository.delete(existing);
    }

    private Product findOrThrow(String id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
