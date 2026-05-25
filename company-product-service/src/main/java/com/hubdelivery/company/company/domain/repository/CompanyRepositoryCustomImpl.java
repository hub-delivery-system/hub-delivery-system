package com.hubdelivery.company.company.domain.repository;

import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.entity.QCompany;
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
public class CompanyRepositoryCustomImpl implements CompanyRepositoryCustom {

    private static final QCompany COMPANY = QCompany.company;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Company> searchCompanies(String keyword, Pageable pageable) {

        BooleanBuilder condition = createSearchCondition(keyword);

        List<Company> content = queryFactory
                .selectFrom(COMPANY)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QueryDslRepositoryUtils.toOrderSpecifiers(
                        pageable.getSort(),
                        this::toOrderSpecifier
                ))
                .fetch();

        Long total = queryFactory
                .select(COMPANY.count())
                .from(COMPANY)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, totalOrZero(total));
    }

    private BooleanBuilder createSearchCondition(String keyword) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(dateTimePath(COMPANY, "deletedAt").isNull());

        if (keyword != null) {
            condition.and(
                    COMPANY.companyName.containsIgnoreCase(keyword)
                            .or(COMPANY.address.containsIgnoreCase(keyword))
            );
        }

        return condition;
    }

    private OrderSpecifier<?> toOrderSpecifier(Order direction, String property) {
        return switch (property) {
            case "updatedAt" -> new OrderSpecifier<>(direction, dateTimePath(COMPANY, "updatedAt"));
            case "companyName" -> new OrderSpecifier<>(direction, COMPANY.companyName);
            case "createdAt" -> new OrderSpecifier<>(direction, dateTimePath(COMPANY, "createdAt"));
            default -> new OrderSpecifier<>(Order.DESC, dateTimePath(COMPANY, "createdAt"));
        };
    }
}
