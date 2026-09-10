package com.infobean.productcatalog.product.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_status", columnList = "status")
})
public class Product {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = ProductConstraints.NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, precision = ProductConstraints.PRICE_PRECISION, scale = ProductConstraints.PRICE_FRACTION_DIGITS)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    protected Product() {
    }

    private Product(UUID id, String name, BigDecimal price, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.status = status;
    }

    public static Product create(String name, BigDecimal price, ProductStatus status) {
        return new Product(UUID.randomUUID(), name, price, status);
    }

    public void update(String name, BigDecimal price, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }
}
