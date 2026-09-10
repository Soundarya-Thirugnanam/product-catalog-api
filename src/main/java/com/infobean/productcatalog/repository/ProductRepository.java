package com.infobean.productcatalog.repository;

import com.infobean.productcatalog.entity.Product;
import com.infobean.productcatalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAllByStatus(
            ProductStatus status,
            Pageable pageable);
}
