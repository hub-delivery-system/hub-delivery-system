package com.hubdelivery.orderservice.order.domain.repository;

import com.hubdelivery.orderservice.order.domain.entity.Order;
import com.hubdelivery.orderservice.order.domain.entity.QOrder;
import com.hubdelivery.orderservice.order.presentation.dto.OrderSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private static final QOrder ORDER = QOrder.order;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> searchOrders(
            OrderSearchCondition cond,
            UUID fixedProducerId,
            Pageable pageable) {

        BooleanBuilder condition = buildCondition(cond, fixedProducerId);

        List<Order> content = queryFactory
                .selectFrom(ORDER)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(ORDER.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(ORDER.count())
                .from(ORDER)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(OrderSearchCondition cond, UUID fixedProducerId) {
        BooleanBuilder builder = new BooleanBuilder();

        // 소프트 딜리트 제외는 항상 적용
        builder.and(ORDER.deletedAt.isNull());

        // DELIVERY_MANAGER·COMPANY_MANAGER 권한: 본인이 요청한 주문만 조회
        if (fixedProducerId != null) {
            builder.and(ORDER.producerId.eq(fixedProducerId));
        }

        if (cond == null) {
            return builder;
        }

        // fixedProducerId가 없을 때만 사용자 지정 producerId 필터 적용
        if (fixedProducerId == null && cond.getProducerId() != null) {
            builder.and(ORDER.producerId.eq(cond.getProducerId()));
        }
        if (cond.getReceiverId() != null) {
            builder.and(ORDER.receiverId.eq(cond.getReceiverId()));
        }
        if (cond.getProductId() != null) {
            builder.and(ORDER.productId.eq(cond.getProductId()));
        }
        if (cond.getStatus() != null) {
            builder.and(ORDER.status.eq(cond.getStatus()));
        }

        return builder;
    }
}
