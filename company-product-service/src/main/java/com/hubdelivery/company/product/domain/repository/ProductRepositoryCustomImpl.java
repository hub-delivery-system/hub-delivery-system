package com.hubdelivery.company.product.domain.repository;

import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.entity.QProduct;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private static final QProduct PRODUCT = QProduct.product;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        BooleanBuilder condition = createSearchCondition(keyword);

        List<Product> content = queryFactory
                .selectFrom(PRODUCT)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(PRODUCT.count())
                .from(PRODUCT)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder createSearchCondition(String keyword) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(dateTimePath("deletedAt").isNull());

        if (keyword != null) {
            condition.and(PRODUCT.productName.containsIgnoreCase(keyword));
        }

        return condition;
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order sortOrder : sort) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
            orderSpecifiers.add(toOrderSpecifier(direction, sortOrder.getProperty()));
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> toOrderSpecifier(Order direction, String property) {
        return switch (property) {
            case "updatedAt" -> new OrderSpecifier<>(direction, dateTimePath("updatedAt"));
            case "productName" -> new OrderSpecifier<>(direction, PRODUCT.productName);
            case "createdAt" -> new OrderSpecifier<>(direction, dateTimePath("createdAt"));
            default -> new OrderSpecifier<>(Order.DESC, dateTimePath("createdAt"));
        };
    }

    private DateTimePath<LocalDateTime> dateTimePath(String property) {
        return Expressions.dateTimePath(LocalDateTime.class, PRODUCT, property);
    }
}
