package com.hubdelivery.company.product.domain.repository;

import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.entity.QProduct;
import com.hubdelivery.company.global.util.QueryDslRepositoryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hubdelivery.company.global.util.QueryDslRepositoryUtils.dateTimePath;
import static com.hubdelivery.company.global.util.QueryDslRepositoryUtils.totalOrZero;

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
                .orderBy(QueryDslRepositoryUtils.toOrderSpecifiers(
                        pageable.getSort(),
                        this::toOrderSpecifier
                ))
                .fetch();

        Long total = queryFactory
                .select(PRODUCT.count())
                .from(PRODUCT)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, totalOrZero(total));
    }

    private BooleanBuilder createSearchCondition(String keyword) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(dateTimePath(PRODUCT, "deletedAt").isNull());

        if (keyword != null) {
            condition.and(PRODUCT.productName.containsIgnoreCase(keyword));
        }

        return condition;
    }

    private OrderSpecifier<?> toOrderSpecifier(Order direction, String property) {
        return switch (property) {
            case "updatedAt" -> new OrderSpecifier<>(direction, dateTimePath(PRODUCT, "updatedAt"));
            case "productName" -> new OrderSpecifier<>(direction, PRODUCT.productName);
            case "createdAt" -> new OrderSpecifier<>(direction, dateTimePath(PRODUCT, "createdAt"));
            default -> new OrderSpecifier<>(Order.DESC, dateTimePath(PRODUCT, "createdAt"));
        };
    }
}
