package com.infobean.productcatalog.dto;

import com.infobean.productcatalog.constants.ProductConstants;
import com.infobean.productcatalog.entity.ProductStatus;
import com.infobean.productcatalog.exception.ErrorMessages;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = ErrorMessages.NAME_MUST_NOT_BE_BLANK)
        @Size(max = ProductConstants.NAME_MAX_LENGTH, message = ErrorMessages.NAME_MUST_NOT_EXCEED_200_CHARACTERS)
        String name,

        @Size(max = ProductConstants.DESCRIPTION_MAX_LENGTH, message = ErrorMessages.DESCRIPTION_MUST_NOT_EXCEED_1000_CHARACTERS)
        String description,

        @NotNull(message = ErrorMessages.PRICE_IS_REQUIRED)
        @DecimalMin(value = ProductConstants.PRICE_MIN_VALUE, inclusive = false, message = ErrorMessages.PRICE_MUST_BE_GREATER_THAN_ZERO)
        @Digits(integer = ProductConstants.PRICE_INTEGER_DIGITS, fraction = ProductConstants.PRICE_FRACTION_DIGITS, message = ErrorMessages.PRICE_MUST_HAVE_AT_MOST_15_INTEGER_DIGITS_AND_4_DECIMAL_PLACES)
        BigDecimal price,

        @NotNull(message = ErrorMessages.STATUS_IS_REQUIRED)
        ProductStatus status
) {
}
