package com.lunazkoe.newsfeed.domain.notification.listener;

import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import com.lunazkoe.newsfeed.domain.notification.entity.NotificationResourceType;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import java.util.UUID;

public record CreateNotificationEvent(
    UUID receiverId,
    String content,
    NotificationResourceType resourceType,
    UUID resourceId
) {

    public static CreateNotificationEvent createByCommentLike(Comment comment, User sender) {
        String content = String.format("[%s]님이 나의 댓글을 좋아합니다.", sender.getNickname());

        return new CreateNotificationEvent(
            comment.getUser().getId(),
            content,
            NotificationResourceType.COMMENT,
            comment.getId()
        );
    }
}
