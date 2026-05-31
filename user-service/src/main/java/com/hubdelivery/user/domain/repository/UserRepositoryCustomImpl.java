package com.hubdelivery.user.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.QUser;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.type.UserStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private static final QUser USER = QUser.user;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> searchUsers(
            String username,
            String name,
            UserRole role,
            UserStatus status,
            UUID hubId,
            UUID companyId,
            Pageable pageable
    ) {
        BooleanBuilder condition = buildCondition(username, name, role, status, hubId, companyId);

        JPAQuery<User> contentQuery = queryFactory.selectFrom(USER).where(condition);
        applySort(pageable, contentQuery);

        List<User> content = contentQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(USER.count())
                .from(USER)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(
            String username,
            String name,
            UserRole role,
            UserStatus status,
            UUID hubId,
            UUID companyId
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(username)) {
            builder.and(USER.username.containsIgnoreCase(username.trim()));
        }
        if (StringUtils.hasText(name)) {
            builder.and(USER.username.containsIgnoreCase(name.trim()));
        }
        if (role != null) {
            builder.and(USER.role.eq(role));
        }
        if (status != null) {
            builder.and(USER.status.eq(status));
        }
        if (hubId != null) {
            builder.and(USER.hubId.eq(hubId));
        }
        if (companyId != null) {
            builder.and(USER.companyId.eq(companyId));
        }

        return builder;
    }

    private void applySort(Pageable pageable, JPAQuery<User> query) {
        if (pageable.getSort().isUnsorted()) {
            query.orderBy(USER.createdAt.desc());
            return;
        }

        for (Sort.Order order : pageable.getSort()) {
            if ("updatedAt".equals(order.getProperty())) {
                query.orderBy(order.isAscending() ? USER.updatedAt.asc() : USER.updatedAt.desc());
                continue;
            }

            query.orderBy(order.isAscending() ? USER.createdAt.asc() : USER.createdAt.desc());
        }
    }
}
