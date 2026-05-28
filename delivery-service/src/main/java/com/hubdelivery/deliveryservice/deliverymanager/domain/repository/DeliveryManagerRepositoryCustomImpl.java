package com.hubdelivery.deliveryservice.deliverymanager.domain.repository;

import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.QDeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DeliveryManagerRepositoryCustomImpl implements DeliveryManagerRepositoryCustom {

    private static final QDeliveryManager DM = QDeliveryManager.deliveryManager;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DeliveryManager> searchManagers(
            DeliveryManagerSearchCondition cond,
            UUID fixedHubId,
            Pageable pageable) {

        BooleanBuilder condition = buildCondition(cond, fixedHubId);

        List<DeliveryManager> content = queryFactory
                .selectFrom(DM)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(DM.sequence.asc())
                .fetch();

        Long total = queryFactory
                .select(DM.count())
                .from(DM)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(DeliveryManagerSearchCondition cond, UUID fixedHubId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(DM.deletedAt.isNull());

        // HUB_MANAGER 권한 강제 필터
        if (fixedHubId != null) {
            builder.and(DM.hubId.eq(fixedHubId));
        }

        if (cond == null) {
            return builder;
        }

        if (cond.getType() != null) {
            builder.and(DM.type.eq(cond.getType()));
        }
        // fixedHubId가 없을 때만 cond.hubId 적용 (중복 필터 방지)
        if (fixedHubId == null && cond.getHubId() != null) {
            builder.and(DM.hubId.eq(cond.getHubId()));
        }
        if (cond.getUserId() != null) {
            builder.and(DM.userId.eq(cond.getUserId()));
        }

        return builder;
    }
}
