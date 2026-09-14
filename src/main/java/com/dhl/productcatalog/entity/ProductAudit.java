package com.dhl.productcatalog.entity;

import com.dhl.productcatalog.constants.ProductConstants;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * An immutable log entry capturing a product's full state at the moment it was
 * created, updated, or deleted. Unlike {@link Product}, which only ever holds the
 * latest value, every change appends a new row here so history is never lost.
 */
@Entity
@Table(name = "product_audit", indexes = {
        @Index(name = "idx_product_audit_product_id", columnList = "product_id")
})
public class ProductAudit {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, updatable = false, length = ProductConstants.NAME_MAX_LENGTH)
    private String name;

    @Column(updatable = false, length = ProductConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false, updatable = false, precision = ProductConstants.PRICE_PRECISION, scale = ProductConstants.PRICE_FRACTION_DIGITS)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private ProductAuditAction action;

    @Column(name = "performed_by", nullable = false, updatable = false, length = 100)
    private String performedBy;

    @Column(name = "performed_on", nullable = false, updatable = false)
    private Instant performedOn;

    /**
     * No-arg constructor required by JPA.
     */
    protected ProductAudit() {
    }

    private ProductAudit(UUID id, UUID productId, String name, String description, BigDecimal price,
                          ProductStatus status, ProductAuditAction action, String performedBy, Instant performedOn) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.action = action;
        this.performedBy = performedBy;
        this.performedOn = performedOn;
    }

    /**
     * Snapshots a product's current state as a new audit log entry.
     */
    public static ProductAudit of(Product product, ProductAuditAction action, String performedBy) {
        return new ProductAudit(
                UUID.randomUUID(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                action,
                performedBy,
                Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public ProductAuditAction getAction() {
        return action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public Instant getPerformedOn() {
        return performedOn;
    }
}
