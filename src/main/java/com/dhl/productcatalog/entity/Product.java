package com.dhl.productcatalog.entity;

import com.dhl.productcatalog.constants.ProductConstants;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "products",
        indexes = {
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_status", columnList = "status")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_product_unique_name", columnNames = "unique_name")
)
public class Product {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = ProductConstants.NAME_MAX_LENGTH)
    private String name;

    /**
     * Mirrors {@code name} while the product is active and is set to {@code null} on soft
     * delete, so the unique constraint stops applying to deleted rows: SQL unique constraints
     * don't consider {@code NULL} values as conflicting, so a deleted product's name frees up
     * for reuse while an active row with that name still can't coexist with another.
     */
    @Column(name = "unique_name", length = ProductConstants.NAME_MAX_LENGTH)
    private String uniqueName;

    @Column(length = ProductConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false, precision = ProductConstants.PRICE_PRECISION, scale = ProductConstants.PRICE_FRACTION_DIGITS)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @CreatedDate
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    private Instant updatedOn;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @Column(nullable = false)
    private boolean deleted = false;

    /**
     * No-arg constructor required by JPA.
     */
    protected Product() {
    }

    /**
     * Backs {@link #create}.
     */
    private Product(UUID id, String name, String description, BigDecimal price, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.uniqueName = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    /**
     * Factory for new products, generating a fresh id.
     */
    public static Product create(String name, String description, BigDecimal price, ProductStatus status) {
        return new Product(UUID.randomUUID(), name, description, price, status);
    }

    /**
     * Applies a full update to an existing product in place.
     */
    public void update(String name, String description, BigDecimal price, ProductStatus status) {
        this.name = name;
        this.uniqueName = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    /**
     * Soft-deletes the product: the row stays, but it's excluded from all read queries,
     * and its name is freed up (see {@link #uniqueName}) for reuse by a new product.
     */
    public void markDeleted() {
        this.deleted = true;
        this.uniqueName = null;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName() {
        return uniqueName;
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

    public Instant getCreatedOn() {
        return createdOn;
    }

    public Instant getUpdatedOn() {
        return updatedOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
