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
        uniqueConstraints = @UniqueConstraint(name = "uk_product_name", columnNames = "name")
)
public class Product {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = ProductConstants.NAME_MAX_LENGTH)
    private String name;

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
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public UUID getId() {
        return id;
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
}
