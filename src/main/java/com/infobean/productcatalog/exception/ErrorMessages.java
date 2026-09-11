package com.infobean.productcatalog.exception;

public final class ErrorMessages {

    public static final String DIRECTION_MUST_BE_ASC_OR_DESC = "direction must be asc or desc";
    public static final String PRODUCT_NOT_FOUND_WITH_ID = "Product not found with id: %s";
    public static final String VALIDATION_FAILED = "Validation failed";

    public static final String NAME_MUST_NOT_BE_BLANK = "name must not be blank";
    public static final String NAME_MUST_NOT_EXCEED_200_CHARACTERS = "name must not exceed 200 characters";
    public static final String DESCRIPTION_MUST_NOT_EXCEED_1000_CHARACTERS = "description must not exceed 1000 characters";
    public static final String PRODUCT_NAME_ALREADY_EXISTS = "Product name already exists: %s";
    public static final String PRICE_IS_REQUIRED = "price is required";
    public static final String PRICE_MUST_BE_GREATER_THAN_ZERO = "price must be greater than zero";
    public static final String PRICE_MUST_HAVE_AT_MOST_15_INTEGER_DIGITS_AND_4_DECIMAL_PLACES =
            "price must have at most 15 integer digits and 4 decimal places";
    public static final String STATUS_IS_REQUIRED = "status is required";
    public static final String MALFORMED_REQUEST_BODY = "Malformed request body";
    public static final String DATA_INTEGRITY_VIOLATION = "Request violates a data integrity constraint";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";

    /**
     * Prevents instantiation.
     */
    private ErrorMessages() {
    }
}
