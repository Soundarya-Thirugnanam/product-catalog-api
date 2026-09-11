package com.infobean.productcatalog.dto;

import com.infobean.productcatalog.entity.Product;
import com.infobean.productcatalog.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        Instant createdOn,
        Instant updatedOn,
        String createdBy,
        String updatedBy
) {
    /**
     * Maps a {@link Product} entity to its API-facing representation.
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                product.getCreatedOn(),
                product.getUpdatedOn(),
                product.getCreatedBy(),
                product.getUpdatedBy()
        );
    }
}
