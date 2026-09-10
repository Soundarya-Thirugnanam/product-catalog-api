package com.infobean.productcatalog.product.application;

import com.infobean.productcatalog.product.domain.Product;
import com.infobean.productcatalog.product.domain.ProductStatus;
import com.infobean.productcatalog.shared.exception.ProductNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.create(
                normalizeName(request.name()),
                request.price(),
                request.status()
        );

        return ProductResponse.from(repository.save(product));
    }

    public ProductResponse getById(UUID id) {
        return repository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Page<ProductResponse> getAll(ProductStatus status, Pageable pageable) {
        Page<Product> products = status == null
                ? repository.findAll(pageable)
                : repository.findAllByStatus(status, pageable);

        return products.map(ProductResponse::from);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.update(
                normalizeName(request.name()),
                request.price(),
                request.status()
        );

        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        repository.delete(product);
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}
