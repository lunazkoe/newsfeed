package com.lunazkoe.newsfeed.domain.commentlike.dto;

import com.lunazkoe.newsfeed.domain.commentlike.entity.CommentLike;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikeDto(
    UUID id,
    UUID likedBy,
    LocalDateTime createdAt,
    UUID commentId,
    UUID articleId,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    long commentLikeCount,
    LocalDateTime commentCreatedAt
) {

    public static CommentLikeDto from(CommentLike commentLike) {
        return new CommentLikeDto(
            commentLike.getId(),
            commentLike.getUser().getId(),
            commentLike.getCreatedAt(),
            commentLike.getComment().getId(),
            commentLike.getComment().getArticle().getId(),
            commentLike.getComment().getUser().getId(),
            commentLike.getComment().getUser().getNickname(),
            commentLike.getComment().getContent(),
            commentLike.getComment().getLikeCount(),
            commentLike.getComment().getCreatedAt()
        );
    }
}
