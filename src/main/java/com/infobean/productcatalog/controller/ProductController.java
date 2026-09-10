package com.infobean.productcatalog.controller;

import com.infobean.productcatalog.constants.ApiConstants;
import com.infobean.productcatalog.dto.ProductRequest;
import com.infobean.productcatalog.dto.ProductResponse;
import com.infobean.productcatalog.entity.ProductStatus;
import com.infobean.productcatalog.exception.ErrorMessages;
import com.infobean.productcatalog.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.PRODUCTS)
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {

        ProductResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create(ApiPaths.PRODUCTS + "/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE) @Min(ApiConstants.MIN_PAGE) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SIZE) @Min(ApiConstants.MIN_SIZE) @Max(ApiConstants.MAX_SIZE) int size,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_DIRECTION) String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.DIRECTION_MUST_BE_ASC_OR_DESC));

        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ProductResponse> products = service.getAll(status, pageable);

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
