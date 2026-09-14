package com.dhl.productcatalog.config;

import com.dhl.productcatalog.constants.ApiConstants;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Resolves the "current user" for {@code @CreatedBy}/{@code @LastModifiedBy} auditing
 * from the {@value ApiConstants#USER_NAME_HEADER} request header, falling back to a
 * dummy default when the header is absent (e.g. no request in scope, or the caller
 * didn't set it).
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String userName = attributes == null
                ? null
                : attributes.getRequest().getHeader(ApiConstants.USER_NAME_HEADER);

        return Optional.of(
                (userName == null || userName.isBlank()) ? ApiConstants.DEFAULT_USER_NAME : userName
        );
    }
}
