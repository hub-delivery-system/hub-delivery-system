package com.hubdelivery.company.global.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public enum SearchSortPolicy {

    COMPANY("createdAt", Set.of("createdAt", "updatedAt", "companyName")),
    PRODUCT("createdAt", Set.of("createdAt", "updatedAt", "productName"));

    @Getter
    private final String defaultSortProperty;

    private final Set<String> allowedSortProperties;

    public boolean isAllowedSortProperty(String property) {
        return allowedSortProperties.contains(property);
    }
}
