package com.infobean.productcatalog.web;

import com.infobean.productcatalog.exception.ErrorMessages;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageableFactory {

    public Pageable create(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.DIRECTION_MUST_BE_ASC_OR_DESC));

        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}
