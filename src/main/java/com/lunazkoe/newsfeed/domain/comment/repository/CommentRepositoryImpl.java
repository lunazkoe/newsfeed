package com.lunazkoe.newsfeed.domain.comment.repository;

import static com.lunazkoe.newsfeed.domain.comment.entity.QComment.comment;

import com.lunazkoe.newsfeed.domain.comment.dto.CommentSearchCondition;
import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
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
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Comment> searchComments(CommentSearchCondition condition) {

        List<Comment> comments = queryFactory
            .selectFrom(comment)
            .where(
                isNotDeleted(),
                searchArticleId(condition.articleId()),
                cursorCondition(condition.orderBy(), condition.direction(), condition.cursor(), condition.after())
            )
            .orderBy(createOrderSpecifier(condition.orderBy(), condition.direction()))
            .limit(condition.limit() + 1)
            .fetch();

        boolean hasNext = comments.size() > condition.limit();
        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext) {
            comments.remove(comments.size() - 1);
        }

        // ==
        if (!comments.isEmpty()) {
            Comment lastComment = comments.get(comments.size() - 1);
            nextAfter = lastComment.getCreatedAt();

            if ("createdAt".equals(condition.orderBy())) {
                nextCursor = lastComment.getCreatedAt() + "_" + lastComment.getId();
            } else if ("likeCount".equals(condition.orderBy())) {
                nextCursor = lastComment.getLikeCount() + "_" + lastComment.getId();
            }
        }

        Long totalElementCount = null;
        if (!StringUtils.hasText(condition.cursor())) {
            totalElementCount = Optional.ofNullable(
                queryFactory
                    .select(comment.count())
                    .from(comment)
                    .where(
                        isNotDeleted(),
                        searchArticleId(condition.articleId())
                    )
                    .fetchOne()
            ).orElse(0L);
        }

        return new CursorPageResponse<>(
            comments,
            nextCursor,
            nextAfter,
            condition.limit(),
            totalElementCount,
            hasNext
        );
    }

    private BooleanExpression isNotDeleted() {
        return comment.isDeleted.eq(false);
    }

    private BooleanExpression searchArticleId(UUID articleId) {
        return comment.article.id.eq(articleId);
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

        if ("createdAt".equals(orderBy)) {
            // primaryCursorValue(LocalDateTime 문자열) 혹은 함께 넘어온 after를 기준으로 삼습니다.
            if (isAsc) {
                return comment.createdAt.gt(after)
                    .or(comment.createdAt.eq(after).and(comment.id.gt(cursorId)));
            } else {
                return comment.createdAt.lt(after)
                    .or(comment.createdAt.eq(after).and(comment.id.lt(cursorId)));
            }
        } else {
            Long cursorCount = Long.valueOf(primaryCursorValue); // likeCount 기준 정렬

            if (isAsc) {
                return comment.likeCount.gt(cursorCount)
                    .or(comment.likeCount.eq(cursorCount).and(comment.createdAt.gt(after)))
                    .or(comment.likeCount.eq(cursorCount).and(comment.createdAt.eq(after)).and(comment.id.gt(cursorId)));
            } else {
                return comment.likeCount.lt(cursorCount)
                    .or(comment.likeCount.eq(cursorCount).and(comment.createdAt.lt(after)))
                    .or(comment.likeCount.eq(cursorCount).and(comment.createdAt.eq(after)).and(comment.id.lt(cursorId)));
            }
        }
    }

    private OrderSpecifier<?>[] createOrderSpecifier(String orderBy, String direction) {
        boolean isAsc = "ASC".equalsIgnoreCase(direction);
        Order order = isAsc ? Order.ASC : Order.DESC;

        if ("createdAt".equals(orderBy)) {
            return new OrderSpecifier[]{
                new OrderSpecifier<>(order, comment.createdAt),
                new OrderSpecifier<>(order, comment.id)
            };
        } else {
            return new OrderSpecifier[]{
                new OrderSpecifier<>(order, comment.likeCount),
                new OrderSpecifier<>(order, comment.createdAt),
                new OrderSpecifier<>(order, comment.id)
            };
        }
    }
}
