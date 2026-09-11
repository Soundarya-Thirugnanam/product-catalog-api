package com.infobean.productcatalog.exception;

public class DuplicateProductNameException extends RuntimeException {

    /**
     * Builds the conflict message from the duplicated name.
     */
    public DuplicateProductNameException(String name) {
        super(String.format(ErrorMessages.PRODUCT_NAME_ALREADY_EXISTS, name));
    }
}
