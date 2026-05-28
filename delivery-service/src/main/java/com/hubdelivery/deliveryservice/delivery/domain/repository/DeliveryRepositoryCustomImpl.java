package com.hubdelivery.deliveryservice.delivery.domain.repository;

import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import com.hubdelivery.deliveryservice.delivery.domain.entity.QDelivery;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliverySearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DeliveryRepositoryCustomImpl implements DeliveryRepositoryCustom {

    private static final QDelivery DELIVERY = QDelivery.delivery;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Delivery> searchDeliveries(
            DeliverySearchCondition cond,
            UUID fixedHubId,
            UUID fixedManagerId,
            Pageable pageable) {

        BooleanBuilder condition = buildCondition(cond, fixedHubId, fixedManagerId);

        List<Delivery> content = queryFactory
                .selectFrom(DELIVERY)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(DELIVERY.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(DELIVERY.count())
                .from(DELIVERY)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(
            DeliverySearchCondition cond,
            UUID fixedHubId,
            UUID fixedManagerId) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(DELIVERY.deletedAt.isNull());

        // HUB_MANAGER 권한: startHubId OR endHubId 중 하나가 담당 허브인 배송만
        if (fixedHubId != null) {
            builder.and(
                    DELIVERY.startHubId.eq(fixedHubId)
                            .or(DELIVERY.endHubId.eq(fixedHubId))
            );
        }

        // DELIVERY_MANAGER 권한: 본인이 담당하는 배송만
        if (fixedManagerId != null) {
            builder.and(DELIVERY.deliveryManagerId.eq(fixedManagerId));
        }

        if (cond == null) {
            return builder;
        }

        if (cond.getStatus() != null) {
            builder.and(DELIVERY.status.eq(cond.getStatus()));
        }
        if (cond.getOrderId() != null) {
            builder.and(DELIVERY.orderId.eq(cond.getOrderId()));
        }
        // fixedHubId가 없을 때만 사용자 지정 허브 필터 적용
        if (fixedHubId == null) {
            if (cond.getStartHubId() != null) {
                builder.and(DELIVERY.startHubId.eq(cond.getStartHubId()));
            }
            if (cond.getEndHubId() != null) {
                builder.and(DELIVERY.endHubId.eq(cond.getEndHubId()));
            }
        }

        return builder;
    }
}
