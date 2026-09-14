package com.dhl.productcatalog.service;

import com.dhl.productcatalog.dto.ProductRequest;
import com.dhl.productcatalog.dto.ProductResponse;
import com.dhl.productcatalog.entity.Product;
import com.dhl.productcatalog.entity.ProductAuditAction;
import com.dhl.productcatalog.entity.ProductStatus;
import com.dhl.productcatalog.exception.DuplicateProductNameException;
import com.dhl.productcatalog.exception.ProductNotFoundException;
import com.dhl.productcatalog.repository.ProductAuditRepository;
import com.dhl.productcatalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.AuditorAware;

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

    @Mock
    private ProductAuditRepository auditRepository;

    @Mock
    private AuditorAware<String> auditorAware;

    private ProductService service;

    /**
     * Constructs the service under test before each test.
     */
    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository, auditRepository, auditorAware);
        lenient().when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));
    }

    /**
     * Name normalization must apply on create.
     */
    @Test
    void shouldCreateProductAndNormalizeName() {
        ProductRequest request =
                new ProductRequest("  Gaming   Laptop  ", "A great laptop", new BigDecimal("999.9900"), ProductStatus.ACTIVE);

        Product saved = Product.create(
                "Gaming Laptop",
                "A great laptop",
                new BigDecimal("999.9900"),
                ProductStatus.ACTIVE
        );

        when(repository.existsByName("Gaming Laptop")).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("Gaming Laptop");
        assertThat(response.description()).isEqualTo("A great laptop");
        assertThat(response.price()).isEqualByComparingTo("999.99");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(repository).save(any(Product.class));
        verify(auditRepository).save(argThat(audit -> audit.getAction() == ProductAuditAction.CREATED));
    }

    /**
     * Creating a product with a name that already exists must be rejected.
     */
    @Test
    void shouldRejectDuplicateNameOnCreate() {
        ProductRequest request =
                new ProductRequest("Existing", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        when(repository.existsByName("Existing")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateProductNameException.class);

        verify(repository, never()).save(any());
    }

    /**
     * An existing product must be updated with the new details.
     */
    @Test
    void shouldUpdateExistingProduct() {
        UUID id = UUID.randomUUID();
        Product product = Product.create(
                "Old",
                "Old description",
                new BigDecimal("10.00"),
                ProductStatus.INACTIVE
        );

        when(repository.findById(id)).thenReturn(Optional.of(product));
        when(repository.existsByNameAndIdNot("New Product", id)).thenReturn(false);
        when(repository.saveAndFlush(any(Product.class))).thenReturn(product);

        ProductResponse response = service.update(
                id,
                new ProductRequest(" New Product ", " New description ", new BigDecimal("20.00"), ProductStatus.ACTIVE)
        );

        assertThat(response.name()).isEqualTo("New Product");
        assertThat(response.description()).isEqualTo("New description");
        assertThat(response.price()).isEqualByComparingTo("20.00");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(auditRepository).save(argThat(audit -> audit.getAction() == ProductAuditAction.UPDATED));
    }

    /**
     * Renaming a product to a name already used by another product must be rejected.
     */
    @Test
    void shouldRejectDuplicateNameOnUpdate() {
        UUID id = UUID.randomUUID();
        Product product = Product.create("Old", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(product));
        when(repository.existsByNameAndIdNot("Taken", id)).thenReturn(true);

        ProductRequest request = new ProductRequest("Taken", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(DuplicateProductNameException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    /**
     * A missing product must throw {@link ProductNotFoundException}.
     */
    @Test
    void shouldThrow404WhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: " + id);
    }

    /**
     * A missing product must not be deleted.
     */
    @Test
    void shouldNotDeleteUnknownProduct() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository, never()).delete(any());
    }

    /**
     * Deleting a product must still record a DELETED audit entry.
     */
    @Test
    void shouldRecordAuditEntryOnDelete() {
        UUID id = UUID.randomUUID();
        Product product = Product.create("Old", null, new BigDecimal("10.00"), ProductStatus.ACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(product));

        service.delete(id);

        verify(auditRepository).save(argThat(audit -> audit.getAction() == ProductAuditAction.DELETED));
        verify(repository).delete(product);
    }
}
