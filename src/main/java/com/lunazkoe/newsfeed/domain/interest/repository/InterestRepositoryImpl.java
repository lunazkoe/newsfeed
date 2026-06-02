package com.lunazkoe.newsfeed.domain.interest.repository;

import static com.lunazkoe.newsfeed.domain.interest.entity.QInterest.interest;
import static com.lunazkoe.newsfeed.domain.interest.entity.QInterestKeyword.interestKeyword;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestSearchCondition;
import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Interest> searchInterests(InterestSearchCondition condition) {

        List<Interest> interests = queryFactory
            .selectFrom(interest)
            // 엔티티 필드명에 맞춰 interest.keywords 로 조인
            .leftJoin(interest.keywords, interestKeyword)
            .where(
                containsKeyword(condition.keyword()),
                cursorCondition(condition.orderBy(), condition.direction(), condition.cursor(), condition.after())
            )
            .orderBy(createOrderSpecifier(condition.orderBy(), condition.direction()))
            .limit(condition.limit() + 1)
            .fetch();

        boolean hasNext = interests.size() > condition.limit();
        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext) {
            interests.remove(interests.size() - 1);
        }

        if (!interests.isEmpty()) {
            Interest lastInterest = interests.get(interests.size() - 1);

            nextAfter = lastInterest.getCreatedAt();

            // UUID는 toString()으로 문자열 변환되어 결합됨 (예: 스포츠_123e4567-e89b-12d3-a456-426614174000)
            if ("name".equals(condition.orderBy())) {
                nextCursor = lastInterest.getName() + "_" + lastInterest.getId();
            } else if ("subscriberCount".equals(condition.orderBy())) {
                nextCursor = lastInterest.getSubscriberCount()+ "_" + lastInterest.getId().toString();
            }
        }

        Long totalElementCount = null;
        if (!StringUtils.hasText(condition.cursor())) {
            totalElementCount = Optional.ofNullable(
                queryFactory
                    .select(interest.countDistinct()) // 조인 중복 카운트 방지
                    .from(interest)
                    .leftJoin(interest.keywords, interestKeyword)
                    .where(containsKeyword(condition.keyword()))
                    .fetchOne()
            ).orElse(0L);
        }

        return new CursorPageResponse<>(
            interests,
            nextCursor,
            nextAfter,
            condition.limit(),
            totalElementCount,
            hasNext
        );
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return interest.name.contains(keyword)
            .or(interestKeyword.keyword.contains(keyword));
    }

    private BooleanExpression cursorCondition(String orderBy, String direction, String cursor, LocalDateTime after) {
        if (!StringUtils.hasText(cursor) || after == null || !cursor.contains("_")) {
            return null;
        }

        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        // 마지막 언더바(_) 기준 분리
        int lastDashIndex = cursor.lastIndexOf("_");
        String primaryCursorValue = cursor.substring(0, lastDashIndex);

        // UUID 타입으로 파싱
        UUID cursorId = UUID.fromString(cursor.substring(lastDashIndex + 1));

        if ("name".equals(orderBy)) {
            if (isAsc) {
                return interest.name.gt(primaryCursorValue)
                    .or(interest.name.eq(primaryCursorValue).and(interest.createdAt.gt(after)))
                    .or(interest.name.eq(primaryCursorValue).and(interest.createdAt.eq(after)).and(interest.id.gt(cursorId)));
            } else {
                return interest.name.lt(primaryCursorValue)
                    .or(interest.name.eq(primaryCursorValue).and(interest.createdAt.lt(after)))
                    .or(interest.name.eq(primaryCursorValue).and(interest.createdAt.eq(after)).and(interest.id.lt(cursorId)));
            }
        } else {
            Long cursorCount = Long.valueOf(primaryCursorValue); // subscriberCount가 long 타입이므로 Long으로 파싱

            if (isAsc) {
                return interest.subscriberCount.gt(cursorCount)
                    .or(interest.subscriberCount.eq(cursorCount).and(interest.createdAt.gt(after)))
                    .or(interest.subscriberCount.eq(cursorCount).and(interest.createdAt.eq(after)).and(interest.id.gt(cursorId)));
            } else {
                return interest.subscriberCount.lt(cursorCount)
                    .or(interest.subscriberCount.eq(cursorCount).and(interest.createdAt.lt(after)))
                    .or(interest.subscriberCount.eq(cursorCount).and(interest.createdAt.eq(after)).and(interest.id.lt(cursorId)));
            }
        }
    }

    private OrderSpecifier<?>[] createOrderSpecifier(String orderBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);
        Order order = isAsc ? Order.ASC : Order.DESC;

        if ("name".equals(orderBy)) {
            return new OrderSpecifier[]{
                new OrderSpecifier<>(order, interest.name),
                new OrderSpecifier<>(order, interest.createdAt),
                new OrderSpecifier<>(order, interest.id)
            };
        } else {
            return new OrderSpecifier[]{
                new OrderSpecifier<>(order, interest.subscriberCount),
                new OrderSpecifier<>(order, interest.createdAt),
                new OrderSpecifier<>(order, interest.id)
            };
        }
    }
}