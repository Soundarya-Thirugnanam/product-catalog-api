package com.infobean.productcatalog.service;

import com.infobean.productcatalog.dto.ProductRequest;
import com.infobean.productcatalog.dto.ProductResponse;
import com.infobean.productcatalog.entity.Product;
import com.infobean.productcatalog.entity.ProductStatus;
import com.infobean.productcatalog.exception.ProductNotFoundException;
import com.infobean.productcatalog.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    /**
     * Creates the service with its repository dependency.
     */
    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and persists a new product from the given request.
     */
    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.create(
                normalizeName(request.name()),
                request.price(),
                request.status()
        );

        return ProductResponse.from(repository.save(product));
    }

    /**
     * Fetches a single product by id.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @Override
    public ProductResponse getById(UUID id) {
        return repository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Lists products, optionally filtered by status.
     */
    @Override
    public Page<ProductResponse> getAll(ProductStatus status, Pageable pageable) {
        Page<Product> products = status == null
                ? repository.findAll(pageable)
                : repository.findAllByStatus(status, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * Updates an existing product with the provided details.
     *
     * @throws ProductNotFoundException if the product does not exist
     */

    @Override
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

    /**
     * Deletes a product by id.
     *
     * @throws ProductNotFoundException if the product does not exist
     */
    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        repository.delete(product);
    }

    /**
     * Trims and collapses repeated whitespace in a product name.
     */
    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}
