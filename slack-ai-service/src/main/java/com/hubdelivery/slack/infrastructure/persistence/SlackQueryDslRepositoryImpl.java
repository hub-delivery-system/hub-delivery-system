package com.hubdelivery.slack.infrastructure.persistence;

import com.hubdelivery.slack.application.dto.SlackMessageResponseDto;
import com.hubdelivery.slack.application.dto.SlackSearchRequestDto;
import com.hubdelivery.slack.domain.entity.QSlackMessage;
import com.hubdelivery.slack.domain.entity.SlackMessage;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class SlackQueryDslRepositoryImpl implements SlackQueryDslRepository {

    // @RequiredArgsConstructor 생성자 주입 대신 @Autowired 필드 주입
    // Spring Data JPA 커스텀 impl은 필드 주입이 안전
    @Autowired
    private JPAQueryFactory queryFactory;

    @Override
    public Page<SlackMessageResponseDto> search(SlackSearchRequestDto requestDto, Pageable pageable) {
        QSlackMessage q = QSlackMessage.slackMessage;

        BooleanExpression condition = q.isNotNull();

        if (requestDto.getMessage() != null && !requestDto.getMessage().isBlank()) {
            condition = condition.and(q.message.containsIgnoreCase(requestDto.getMessage()));
        }
        if (requestDto.getUserId() != null) {
            condition = condition.and(q.userId.eq(requestDto.getUserId()));
        }
        if (requestDto.getSlackId() != null && !requestDto.getSlackId().isBlank()) {
            condition = condition.and(q.slackId.eq(requestDto.getSlackId()));
        }

        List<SlackMessage> result = queryFactory
                .selectFrom(q)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(q.createdAt.desc())
                .fetch();

        Long totalCount = queryFactory
                .select(q.count())
                .from(q)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(
                result.stream().map(SlackMessageResponseDto::new).toList(),
                pageable,
                totalCount != null ? totalCount : 0L
        );
    }
}