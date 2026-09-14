package com.dhl.productcatalog.service;

import com.dhl.productcatalog.constants.ApiConstants;
import com.dhl.productcatalog.dto.ProductAuditResponse;
import com.dhl.productcatalog.dto.ProductRequest;
import com.dhl.productcatalog.dto.ProductResponse;
import com.dhl.productcatalog.entity.Product;
import com.dhl.productcatalog.entity.ProductAudit;
import com.dhl.productcatalog.entity.ProductAuditAction;
import com.dhl.productcatalog.entity.ProductStatus;
import com.dhl.productcatalog.exception.DuplicateProductNameException;
import com.dhl.productcatalog.exception.ProductNotFoundException;
import com.dhl.productcatalog.repository.ProductAuditRepository;
import com.dhl.productcatalog.repository.ProductRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductAuditRepository auditRepository;
    private final AuditorAware<String> auditorAware;

    /**
     * Creates the service with its repository dependencies.
     */
    public ProductServiceImpl(ProductRepository repository, ProductAuditRepository auditRepository,
                               AuditorAware<String> auditorAware) {
        this.repository = repository;
        this.auditRepository = auditRepository;
        this.auditorAware = auditorAware;
    }

    /**
     * Creates and persists a new product from the given request.
     *
     * @throws DuplicateProductNameException if a product with this name already exists
     */
    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        String name = normalizeName(request.name());

        if (repository.existsByName(name)) {
            throw new DuplicateProductNameException(name);
        }

        Product product = Product.create(
                name,
                normalizeDescription(request.description()),
                request.price(),
                request.status()
        );

        Product saved = repository.save(product);
        recordAudit(saved, ProductAuditAction.CREATED);

        return ProductResponse.from(saved);
    }

    /**
     * Fetches a single product by id.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @Override
    public ProductResponse getById(UUID id) {
        return repository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Lists products, optionally filtered by status.
     */
    @Override
    public Page<ProductResponse> getAll(ProductStatus status, Pageable pageable) {
        Page<Product> products = status == null
                ? repository.findAll(pageable)
                : repository.findAllByStatus(status, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * Updates an existing product with the provided details.
     *
     * @throws ProductNotFoundException      if the product does not exist
     * @throws DuplicateProductNameException if another product already has this name
     */
    @Override
    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String name = normalizeName(request.name());

        if (repository.existsByNameAndIdNot(name, id)) {
            throw new DuplicateProductNameException(name);
        }

        product.update(
                name,
                normalizeDescription(request.description()),
                request.price(),
                request.status()
        );

        Product updated = repository.saveAndFlush(product);
        recordAudit(updated, ProductAuditAction.UPDATED);

        return ProductResponse.from(updated);
    }

    /**
     * Deletes a product by id.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        recordAudit(product, ProductAuditAction.DELETED);
        repository.delete(product);
    }

    /**
     * Lists a product's full change history, most recent first.
     */
    @Override
    public List<ProductAuditResponse> getAuditHistory(UUID productId) {
        return auditRepository.findAllByProductIdOrderByPerformedOnDesc(productId).stream()
                .map(ProductAuditResponse::from)
                .toList();
    }

    /**
     * Appends an audit log entry snapshotting the product's current state.
     */
    private void recordAudit(Product product, ProductAuditAction action) {
        String performedBy = auditorAware.getCurrentAuditor().orElse(ApiConstants.DEFAULT_USER_NAME);
        auditRepository.save(ProductAudit.of(product, action, performedBy));
    }

    /**
     * Trims and collapses repeated whitespace in a product name.
     */
    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Trims an optional description, leaving {@code null} as-is.
     */
    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }
}
