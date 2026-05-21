package com.hubdelivery.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    public static final String SYSTEM_AUDITOR = "SYSTEM";
    public static final String X_USER_ID = "X-User-Id";

    @Override
    public Optional<String> getCurrentAuditor() {
        return currentRequest()
                .map(request -> request.getHeader(X_USER_ID))
                .filter(userId -> !userId.isBlank())
                .or(() -> Optional.of(SYSTEM_AUDITOR));
    }

    private Optional<HttpServletRequest> currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return Optional.of(servletAttributes.getRequest());
        }
        return Optional.empty();
    }
}
