package com.infobean.productcatalog.repository;

import com.infobean.productcatalog.entity.ProductAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductAuditRepository extends JpaRepository<ProductAudit, UUID> {

    /**
     * Finds a product's full change history, most recent first.
     */
    List<ProductAudit> findAllByProductIdOrderByPerformedOnDesc(UUID productId);
}
