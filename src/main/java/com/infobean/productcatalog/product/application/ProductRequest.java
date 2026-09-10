package com.infobean.productcatalog.product.application;

import com.infobean.productcatalog.product.domain.ProductConstraints;
import com.infobean.productcatalog.product.domain.ProductStatus;
import com.infobean.productcatalog.shared.exception.ErrorMessages;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = ErrorMessages.NAME_MUST_NOT_BE_BLANK)
        @Size(max = ProductConstraints.NAME_MAX_LENGTH, message = ErrorMessages.NAME_MUST_NOT_EXCEED_200_CHARACTERS)
        String name,

        @NotNull(message = ErrorMessages.PRICE_IS_REQUIRED)
        @DecimalMin(value = ProductConstraints.PRICE_MIN_VALUE, inclusive = false, message = ErrorMessages.PRICE_MUST_BE_GREATER_THAN_ZERO)
        @Digits(integer = ProductConstraints.PRICE_INTEGER_DIGITS, fraction = ProductConstraints.PRICE_FRACTION_DIGITS, message = ErrorMessages.PRICE_MUST_HAVE_AT_MOST_15_INTEGER_DIGITS_AND_4_DECIMAL_PLACES)
        BigDecimal price,

        @NotNull(message = ErrorMessages.STATUS_IS_REQUIRED)
        ProductStatus status
) {
}
