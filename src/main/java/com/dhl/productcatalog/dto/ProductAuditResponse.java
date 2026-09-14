package com.dhl.productcatalog.dto;

import com.dhl.productcatalog.entity.ProductAudit;
import com.dhl.productcatalog.entity.ProductAuditAction;
import com.dhl.productcatalog.entity.ProductStatus;

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
