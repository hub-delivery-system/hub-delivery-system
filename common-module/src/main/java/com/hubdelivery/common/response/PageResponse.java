package com.hubdelivery.common.response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort
) {

    private static final String DEFAULT_SORT = "createdAt";
    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                toSortString(page.getSort())
        );
    }

    private static String toSortString(Sort sort) {
        if (sort.isUnsorted()) {
            return DEFAULT_SORT + ", " + DEFAULT_DIRECTION;
        }

        return sort.stream()
                .map(order -> order.getProperty() + ", " + order.getDirection().name())
                .reduce((a, b) -> a + ";" + b)
                .orElse(DEFAULT_SORT + ", " + DEFAULT_DIRECTION);
    }
}
