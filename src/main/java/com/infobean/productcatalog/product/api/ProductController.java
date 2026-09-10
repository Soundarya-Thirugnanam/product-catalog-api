package com.infobean.productcatalog.product.api;

import com.infobean.productcatalog.product.application.ProductRequest;
import com.infobean.productcatalog.product.application.ProductResponse;
import com.infobean.productcatalog.product.application.ProductService;
import com.infobean.productcatalog.product.domain.ProductStatus;
import com.infobean.productcatalog.shared.constants.ApiPaths;
import com.infobean.productcatalog.shared.exception.ErrorMessages;
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
public class
ProductController {

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
            @RequestParam(defaultValue = PaginationDefaults.DEFAULT_PAGE) @Min(PaginationDefaults.MIN_PAGE) int page,
            @RequestParam(defaultValue = PaginationDefaults.DEFAULT_SIZE) @Min(PaginationDefaults.MIN_SIZE) @Max(PaginationDefaults.MAX_SIZE) int size,
            @RequestParam(defaultValue = PaginationDefaults.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationDefaults.DEFAULT_DIRECTION) String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.DIRECTION_MUST_BE_ASC_OR_DESC));

        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(service.getAll(status, pageable));
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
