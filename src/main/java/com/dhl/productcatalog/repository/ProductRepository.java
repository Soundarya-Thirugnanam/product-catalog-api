package com.dhl.productcatalog.repository;

import com.dhl.productcatalog.entity.Product;
import com.dhl.productcatalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Finds a non-deleted product by id.
     */
    Optional<Product> findByIdAndDeletedFalse(UUID id);

    /**
     * Finds non-deleted products with pagination.
     */
    Page<Product> findAllByDeletedFalse(Pageable pageable);

    /**
     * Finds non-deleted products by status with pagination.
     */
    Page<Product> findAllByStatusAndDeletedFalse(
            ProductStatus status,
            Pageable pageable);

    /**
     * Checks whether an active (non-deleted) product with this name already exists.
     */
    boolean existsByUniqueName(String uniqueName);

    /**
     * Checks whether an active (non-deleted) product other than the given id already has this name.
     */
    boolean existsByUniqueNameAndIdNot(String uniqueName, UUID id);
}
