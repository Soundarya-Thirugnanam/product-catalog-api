package com.infobean.productcatalog.constants;

public final class ProductConstants {

    public static final int NAME_MAX_LENGTH = 200;

    public static final String PRICE_MIN_VALUE = "0.00";
    public static final int PRICE_INTEGER_DIGITS = 15;
    public static final int PRICE_FRACTION_DIGITS = 4;
    public static final int PRICE_PRECISION = PRICE_INTEGER_DIGITS + PRICE_FRACTION_DIGITS;

    private ProductConstants() {
    }
}
