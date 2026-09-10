package com.infobean.productcatalog.product.application;

import com.infobean.productcatalog.product.domain.Product;
import com.infobean.productcatalog.product.domain.ProductStatus;
import com.infobean.productcatalog.shared.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository);
    }

    @Test
    void shouldCreateProductAndNormalizeName() {
        ProductRequest request =
                new ProductRequest("  Gaming   Laptop  ", new BigDecimal("999.9900"), ProductStatus.ACTIVE);

        Product saved = Product.create(
                "Gaming Laptop",
                new BigDecimal("999.9900"),
                ProductStatus.ACTIVE
        );

        when(repository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("Gaming Laptop");
        assertThat(response.price()).isEqualByComparingTo("999.99");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(repository).save(any(Product.class));
    }

    @Test
    void shouldUpdateExistingProduct() {
        UUID id = UUID.randomUUID();
        Product product = Product.create(
                "Old",
                new BigDecimal("10.00"),
                ProductStatus.INACTIVE
        );

        when(repository.findById(id)).thenReturn(Optional.of(product));

        ProductResponse response = service.update(
                id,
                new ProductRequest(" New Product ", new BigDecimal("20.00"), ProductStatus.ACTIVE)
        );

        assertThat(response.name()).isEqualTo("New Product");
        assertThat(response.price()).isEqualByComparingTo("20.00");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void shouldThrow404WhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: " + id);
    }

    @Test
    void shouldNotDeleteUnknownProduct() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository, never()).delete(any());
    }
}
