package com.hubdelivery.common.util;

import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageableUtils {

    private static final String DEFAULT_SORT = "createdAt";
    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    public static final int DEFAULT_SIZE = 10;
    public static final int SIZE_30 = 30;
    public static final int SIZE_50 = 50;
    public static final Set<Integer> ALLOWED_SIZES = Set.of(DEFAULT_SIZE, SIZE_30, SIZE_50);

    public static Pageable createPageable(int page, Integer size) {
        int validatedPage = Math.max(page, 0);
        int validatedSize = validateSize(size);

        return PageRequest.of(
                validatedPage,
                validatedSize,
                Sort.by(DEFAULT_DIRECTION, DEFAULT_SORT)
        );
    }

    public static Pageable applyPageSizePolicy(Pageable pageable) {
        int validatedSize = validateSize(pageable.getPageSize());

        if (validatedSize == pageable.getPageSize()) {
            return pageable;
        }

        return PageRequest.of(pageable.getPageNumber(), validatedSize, pageable.getSort());
    }

    public static int validateSize(Integer size) {
        if (ALLOWED_SIZES.contains(size)) {
            return size;
        }

        return DEFAULT_SIZE;
    }

    public static boolean hasKeyword(String keyword) {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
