package com.infobean.productcatalog.controller;

import com.infobean.productcatalog.constants.ApiConstants;
import com.infobean.productcatalog.dto.ProductRequest;
import com.infobean.productcatalog.dto.ProductResponse;
import com.infobean.productcatalog.entity.ProductStatus;
import com.infobean.productcatalog.exception.ProductNotFoundException;
import com.infobean.productcatalog.service.ProductService;
import com.infobean.productcatalog.web.PageableFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.PRODUCTS)
public class ProductController {

    private final ProductService service;
    private final PageableFactory pageableFactory;

    /**
     * Creates the controller with its service dependencies.
     */
    public ProductController(ProductService service, PageableFactory pageableFactory) {
        this.service = service;
        this.pageableFactory = pageableFactory;
    }

    /**
     * Creates a new product.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {

        ProductResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create(ApiPaths.PRODUCTS + "/" + response.id()))
                .body(response);
    }

    /**
     * Fetches a single product by id.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Lists products, optionally filtered by status.
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE) @Min(ApiConstants.MIN_PAGE) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SIZE) @Min(ApiConstants.MIN_SIZE) @Max(ApiConstants.MAX_SIZE) int size,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_DIRECTION) String direction) {

        Pageable pageable = pageableFactory.create(page, size, sortBy, direction);

        Page<ProductResponse> products = service.getAll(status, pageable);

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(products);
    }

    /**
     * Replaces an existing product with the provided details.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Deletes a product by id.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
