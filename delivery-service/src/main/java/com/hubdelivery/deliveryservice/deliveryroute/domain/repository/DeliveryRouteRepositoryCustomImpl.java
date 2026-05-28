package com.hubdelivery.deliveryservice.deliveryroute.domain.repository;

import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.QDeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DeliveryRouteRepositoryCustomImpl implements DeliveryRouteRepositoryCustom {

    private static final QDeliveryRoute ROUTE = QDeliveryRoute.deliveryRoute;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DeliveryRoute> searchRoutes(
            UUID deliveryId,
            DeliveryRouteSearchCondition cond,
            UUID fixedHubId,
            UUID fixedManagerId,
            Pageable pageable) {

        BooleanBuilder condition = buildCondition(deliveryId, cond, fixedHubId, fixedManagerId);

        List<DeliveryRoute> content = queryFactory
                .selectFrom(ROUTE)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(ROUTE.sequence.asc())
                .fetch();

        Long total = queryFactory
                .select(ROUTE.count())
                .from(ROUTE)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(
            UUID deliveryId,
            DeliveryRouteSearchCondition cond,
            UUID fixedHubId,
            UUID fixedManagerId) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(ROUTE.deliveryId.eq(deliveryId));
        builder.and(ROUTE.deletedAt.isNull());

        // HUB_MANAGER 권한: startHubId OR endHubId 중 하나가 담당 허브인 경로만
        if (fixedHubId != null) {
            builder.and(
                    ROUTE.startHubId.eq(fixedHubId)
                            .or(ROUTE.endHubId.eq(fixedHubId))
            );
        }

        // DELIVERY_MANAGER 권한: 본인이 담당하는 경로만
        if (fixedManagerId != null) {
            builder.and(ROUTE.deliveryManagerId.eq(fixedManagerId));
        }

        if (cond == null) {
            return builder;
        }

        if (cond.getStatus() != null) {
            builder.and(ROUTE.status.eq(cond.getStatus()));
        }
        // fixedHubId가 없을 때만 사용자 지정 허브 필터 적용
        if (fixedHubId == null) {
            if (cond.getStartHubId() != null) {
                builder.and(ROUTE.startHubId.eq(cond.getStartHubId()));
            }
            if (cond.getEndHubId() != null) {
                builder.and(ROUTE.endHubId.eq(cond.getEndHubId()));
            }
        }
        if (cond.getDeliveryManagerId() != null && fixedManagerId == null) {
            builder.and(ROUTE.deliveryManagerId.eq(cond.getDeliveryManagerId()));
        }

        return builder;
    }
}
