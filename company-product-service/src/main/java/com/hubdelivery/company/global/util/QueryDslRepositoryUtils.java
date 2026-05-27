package com.hubdelivery.company.global.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryDslRepositoryUtils {

    public static OrderSpecifier<?>[] toOrderSpecifiers(
            Sort sort,
            BiFunction<Order, String, OrderSpecifier<?>> orderSpecifierResolver
    ) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order sortOrder : sort) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
            orderSpecifiers.add(orderSpecifierResolver.apply(direction, sortOrder.getProperty()));
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

    public static DateTimePath<LocalDateTime> dateTimePath(Path<?> parent, String property) {
        return Expressions.dateTimePath(LocalDateTime.class, parent, property);
    }

    public static long totalOrZero(Long total) {
        return total == null ? 0 : total;
    }
}
