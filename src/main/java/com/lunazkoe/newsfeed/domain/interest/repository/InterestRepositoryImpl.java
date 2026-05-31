package com.lunazkoe.newsfeed.domain.interest.repository;

import static com.lunazkoe.newsfeed.domain.interest.entity.QInterest.*;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestSearchCondition;
import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.domain.interest.entity.QInterest;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Interest> searchInterests(InterestSearchCondition condition) {
        List<Interest> interests = queryFactory
            .selectFrom(interest)
            .where()
            .orderBy()
            .limit(condition.limit() + 1)
            .fetch();

        boolean hasNext = interests.size() > condition.limit();
        UUID nextCursor = null;
        String nextAfter = null;

        if (hasNext) {
            interests.remove(interests.size() - 1); // 최과분 1개 제거

            Interest lastInterest = interests.get(interests.size() - 1);
            nextCursor = lastInterest.getId();
            nextAfter = lastInterest.getCreatedAt().toString();
        }

        // cursor가 없을 때 (첫 페이지 요청일 때만)
        // - 첫 페이지가 아닐 경우는 그냥 null로 보내서 처리
        Long totalElementCount = null;
        if (!StringUtils.hasText(condition.cursor())) {
            totalElementCount = Optional.ofNullable(
                queryFactory
                    .select(interest.count())
                    .from(interest)
                    .where()
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


}
