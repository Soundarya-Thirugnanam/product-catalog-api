package com.infobean.productcatalog.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    /**
     * Builds the not-found message from the missing id.
     */
    public ProductNotFoundException(UUID id) {
        super(String.format(ErrorMessages.PRODUCT_NOT_FOUND_WITH_ID, id));
    }
}
