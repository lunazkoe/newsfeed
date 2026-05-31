package com.lunazkoe.newsfeed.domain.notification.repository;

import static com.lunazkoe.newsfeed.domain.notification.entity.QNotification.notification;

import com.lunazkoe.newsfeed.domain.notification.dto.NotificationSearchCondition;
import com.lunazkoe.newsfeed.domain.notification.entity.Notification;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Notification> searchNotifications(NotificationSearchCondition condition, UUID requestUserId) {

        List<Notification> notifications = queryFactory
            .selectFrom(notification)
            .where(
                notification.user.id.eq(requestUserId),
                notification.confirmed.eq(false),
                cursorCondition(condition.cursor(), condition.after())
            )
            .orderBy(notification.createdAt.desc(), notification.id.desc())
            .limit(condition.limit() + 1)
            .fetch();

        boolean hasNext = notifications.size() > condition.limit();
        UUID nextCursor = null;
        String nextAfter = null;

        if (hasNext) {
            notifications.remove(notifications.size() - 1);

            Notification lastNotification = notifications.get(notifications.size() - 1);
            nextCursor = lastNotification.getId();
            nextAfter = lastNotification.getCreatedAt().toString();
        }

        Long totalElementCount = null;
        if (condition.cursor() == null) {
            totalElementCount = Optional.ofNullable(
                queryFactory
                    .select(notification.count())
                    .from(notification)
                    .where(
                        notification.user.id.eq(requestUserId),
                        notification.confirmed.eq(false)
                    )
                    .fetchOne()
            ).orElse(0L);
        }

        return new CursorPageResponse<>(
            notifications,
            nextCursor,
            nextAfter,
            condition.limit(),
            totalElementCount,
            hasNext
        );
    }

    private BooleanExpression cursorCondition(UUID cursor, LocalDateTime after) {
        if (cursor == null || after == null) {
            return null;
        }

        return notification.createdAt.lt(after)
            .or(notification.createdAt.eq(after).and(notification.id.lt(cursor)));
    }
}
