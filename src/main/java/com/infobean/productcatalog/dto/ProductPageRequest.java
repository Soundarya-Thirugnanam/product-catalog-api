package com.infobean.productcatalog.dto;

import com.infobean.productcatalog.constants.ApiConstants;
import com.infobean.productcatalog.exception.ErrorMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ProductPageRequest(
        @Min(ApiConstants.MIN_PAGE) int page,
        @Min(ApiConstants.MIN_SIZE) @Max(ApiConstants.MAX_SIZE) int size,
        String sortBy,
        String direction
) {

    public Pageable toPageable() {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.DIRECTION_MUST_BE_ASC_OR_DESC));

        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}
