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

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

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

    @Override
    public ProductResponse getById(UUID id) {
        return repository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Page<ProductResponse> getAll(ProductStatus status, Pageable pageable) {
        Page<Product> products = status == null
                ? repository.findAll(pageable)
                : repository.findAllByStatus(status, pageable);

        return products.map(ProductResponse::from);
    }

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

    @Override
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
