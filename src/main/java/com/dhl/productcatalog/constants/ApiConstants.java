package com.dhl.productcatalog.constants;

public final class ApiConstants {

    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "20";
    public static final String DEFAULT_SORT_BY = "name";
    public static final String DEFAULT_DIRECTION = "asc";

    public static final int MIN_PAGE = 0;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;

    /**
     * Header a caller (e.g. a Postman collection) can set to identify who is making the
     * request, so it can be recorded in {@code createdBy}/{@code updatedBy} audit columns.
     */
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String DEFAULT_USER_NAME = "postman-user";

    /**
     * Redis cache holding individual product lookups by id.
     */
    public static final String PRODUCT_CACHE = "products";
    public static final long PRODUCT_CACHE_TTL_MINUTES = 10;

    /**
     * Prevents instantiation.
     */
    private ApiConstants() {
    }
}
