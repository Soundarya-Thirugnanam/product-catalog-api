package com.infobean.productcatalog.shared.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID id) {
        super(String.format(ErrorMessages.PRODUCT_NOT_FOUND_WITH_ID, id));
    }
}
