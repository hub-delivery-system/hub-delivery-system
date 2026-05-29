package com.hubdelivery.deliveryservice.delivery.domain.repository;

import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import com.hubdelivery.deliveryservice.delivery.domain.entity.QDelivery;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliverySearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
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

        // DELIVERY_MANAGER 권한: 본인이 담당하는 배송만 (X-User-Id 헤더 기반으로 서비스 계층에서 고정)
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

        // MASTER 권한에서 deliveryManagerId 쿼리 파라미터를 직접 지정한 경우에만 적용.
        // fixedManagerId가 이미 있으면 담당자 고정이 완료된 상태이므로 중복 적용하지 않는다.
        if (fixedManagerId == null && cond.getDeliveryManagerId() != null) {
            builder.and(DELIVERY.deliveryManagerId.eq(cond.getDeliveryManagerId()));
        }

        // 날짜 필터: 지정된 날짜의 자정(00:00) 이상 ~ 익일 자정 미만으로 createdAt 범위를 한정.
        // slack-service 스케줄러가 당일 배송만 추출할 때 사용한다.
        if (cond.getDate() != null) {
            LocalDateTime startOfDay = cond.getDate().atStartOfDay();
            LocalDateTime startOfNextDay = cond.getDate().plusDays(1).atStartOfDay();
            builder.and(DELIVERY.createdAt.goe(startOfDay));
            builder.and(DELIVERY.createdAt.lt(startOfNextDay));
        }

        return builder;
    }
}
