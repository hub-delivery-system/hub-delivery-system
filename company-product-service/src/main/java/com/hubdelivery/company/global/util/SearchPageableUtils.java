package com.hubdelivery.company.global.util;

import com.hubdelivery.common.util.PageableUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SearchPageableUtils {

    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    public static Pageable createPageable(
            Integer page,
            Integer size,
            String sort,
            SearchSortPolicy sortPolicy
    ) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? PageableUtils.DEFAULT_SIZE : size;
        Pageable pageable = PageableUtils.createPageable(pageNumber, pageSize);

        Sort resolvedSort = PageableUtils.hasKeyword(sort)
                ? resolveSort(sort, sortPolicy)
                : defaultSort(sortPolicy);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolvedSort);
    }

    public static String normalizeKeyword(String keyword) {
        if (!PageableUtils.hasKeyword(keyword)) {
            return null;
        }

        return keyword.trim();
    }

    private static Sort resolveSort(String sort, SearchSortPolicy sortPolicy) {
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (!sortPolicy.isAllowedSortProperty(property)) {
            property = sortPolicy.getDefaultSortProperty();
        }

        Sort.Direction direction = parts.length < 2
                ? DEFAULT_SORT_DIRECTION
                : Sort.Direction.fromOptionalString(parts[1].trim()).orElse(DEFAULT_SORT_DIRECTION);

        return Sort.by(direction, property);
    }

    private static Sort defaultSort(SearchSortPolicy sortPolicy) {
        return Sort.by(DEFAULT_SORT_DIRECTION, sortPolicy.getDefaultSortProperty());
    }
}
