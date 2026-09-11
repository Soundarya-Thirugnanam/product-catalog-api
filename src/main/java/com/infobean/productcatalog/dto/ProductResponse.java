package com.infobean.productcatalog.dto;

import com.infobean.productcatalog.entity.Product;
import com.infobean.productcatalog.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        ProductStatus status
) {
    /**
     * Maps a {@link Product} entity to its API-facing representation.
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus()
        );
    }
}
