package com.tenacious.ecommerce.product.service;

import com.tenacious.ecommerce.product.dto.ProductRequest;
import com.tenacious.ecommerce.product.dto.ProductResponse;
import com.tenacious.ecommerce.product.exception.DuplicateSkuException;
import com.tenacious.ecommerce.product.exception.ProductNotFoundException;
import com.tenacious.ecommerce.product.model.Product;
import com.tenacious.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    private final ProductMapper productMapper = new ProductMapper();

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest request;
    private Product entity;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, productMapper);
        request = new ProductRequest("SKU-100", "Wireless Mouse", "Ergonomic wireless mouse",
                "Electronics", new BigDecimal("19.99"), 50);
        entity = productMapper.toEntity(request);
        entity.setId("abc123");
    }

    @Test
    void create_savesProduct_whenSkuIsUnique() {
        when(productRepository.existsBySku("SKU-100")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(entity);

        ProductResponse response = productService.create(request);

        assertThat(response.sku()).isEqualTo("SKU-100");
        assertThat(response.price()).isEqualByComparingTo("19.99");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_throwsDuplicateSkuException_whenSkuAlreadyExists() {
        when(productRepository.existsBySku("SKU-100")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("SKU-100");
    }

    @Test
    void getById_throwsNotFound_whenProductMissing() {
        when(productRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById("missing-id"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getById_returnsProduct_whenFound() {
        when(productRepository.findById("abc123")).thenReturn(Optional.of(entity));

        ProductResponse response = productService.getById("abc123");

        assertThat(response.id()).isEqualTo("abc123");
        assertThat(response.name()).isEqualTo("Wireless Mouse");
    }

    @Test
    void delete_removesProduct_whenFound() {
        when(productRepository.findById("abc123")).thenReturn(Optional.of(entity));

        productService.delete("abc123");

        verify(productRepository).delete(entity);
    }
}
