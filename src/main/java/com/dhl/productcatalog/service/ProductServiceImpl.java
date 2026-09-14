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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

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

        if (repository.existsByUniqueName(name)) {
            log.warn("Rejected create: name={} already exists", name);
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
        log.info("Created product id={} name={}", saved.getId(), saved.getName());

        return ProductResponse.from(saved);
    }

    /**
     * Fetches a single product by id.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @Override
    @Cacheable(cacheNames = ApiConstants.PRODUCT_CACHE, key = "#id")
    public ProductResponse getById(UUID id) {
        return repository.findByIdAndDeletedFalse(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Lists non-deleted products, optionally filtered by status.
     */
    @Override
    public Page<ProductResponse> getAll(ProductStatus status, Pageable pageable) {
        Page<Product> products = status == null
                ? repository.findAllByDeletedFalse(pageable)
                : repository.findAllByStatusAndDeletedFalse(status, pageable);

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
    @CacheEvict(cacheNames = ApiConstants.PRODUCT_CACHE, key = "#id")
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String name = normalizeName(request.name());

        if (repository.existsByUniqueNameAndIdNot(name, id)) {
            log.warn("Rejected update: id={} name={} already used by another product", id, name);
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
        log.info("Updated product id={} name={}", updated.getId(), updated.getName());

        return ProductResponse.from(updated);
    }

    /**
     * Soft-deletes a product by id: the row is kept (marked deleted) so audit history
     * remains available, but the product no longer appears in {@link #getById} or {@link #getAll}.
     *
     * @throws ProductNotFoundException if the product does not exist (or is already deleted)
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = ApiConstants.PRODUCT_CACHE, key = "#id")
    public void delete(UUID id) {
        Product product = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        recordAudit(product, ProductAuditAction.DELETED);
        product.markDeleted();
        repository.save(product);
        log.info("Soft-deleted product id={} name={}", product.getId(), product.getName());
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
