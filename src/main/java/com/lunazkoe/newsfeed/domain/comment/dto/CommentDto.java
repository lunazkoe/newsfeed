package com.lunazkoe.newsfeed.domain.comment.dto;

import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID articleId,
    UUID userId,
    String userNickname,
    String content,
    long likeCount,
    boolean likedByMe,
    LocalDateTime createdAt
) {

    public static CommentDto from(Comment comment, boolean likedByMe) {
        return new CommentDto(
            comment.getId(),
            comment.getArticle().getId(),
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getContent(),
            comment.getLikeCount(),
            likedByMe,
            comment.getCreatedAt()
        );
    }
}