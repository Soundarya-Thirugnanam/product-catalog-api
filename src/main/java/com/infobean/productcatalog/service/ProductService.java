package com.infobean.productcatalog.service;

import com.infobean.productcatalog.dto.ProductRequest;
import com.infobean.productcatalog.dto.ProductResponse;
import com.infobean.productcatalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    /**
     * Persists a new product from the given request.
     */
    ProductResponse create(ProductRequest request);

    /**
     * Looks up a single product by id.
     */
    ProductResponse getById(UUID id);

    /**
     * Lists products, optionally filtered by status.
     */
    Page<ProductResponse> getAll(ProductStatus status, Pageable pageable);

    /**
     * Overwrites an existing product's fields with the given request.
     */
    ProductResponse update(UUID id, ProductRequest request);

    /**
     * Removes a product by id.
     */
    void delete(UUID id);
}
