package com.infobean.productcatalog.product.application;

import com.infobean.productcatalog.product.domain.Product;
import com.infobean.productcatalog.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAllByStatus(
            ProductStatus status,
            Pageable pageable);
}
