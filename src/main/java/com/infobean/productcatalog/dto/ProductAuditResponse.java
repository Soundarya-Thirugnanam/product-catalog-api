package com.infobean.productcatalog.dto;

import com.infobean.productcatalog.entity.ProductAudit;
import com.infobean.productcatalog.entity.ProductAuditAction;
import com.infobean.productcatalog.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductAuditResponse(
        UUID id,
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        ProductAuditAction action,
        String performedBy,
        Instant performedOn
) {
    /**
     * Maps a {@link ProductAudit} entity to its API-facing representation.
     */
    public static ProductAuditResponse from(ProductAudit audit) {
        return new ProductAuditResponse(
                audit.getId(),
                audit.getProductId(),
                audit.getName(),
                audit.getDescription(),
                audit.getPrice(),
                audit.getStatus(),
                audit.getAction(),
                audit.getPerformedBy(),
                audit.getPerformedOn()
        );
    }
}
