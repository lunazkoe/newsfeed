package com.lunazkoe.newsfeed.domain.article.dto;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleDto(
    UUID id,
    String source,
    String sourceUrl,
    String title,
    LocalDateTime publishDate,
    long commentCount,
    long viewCount,
    boolean viewedByMe
) {

    public static ArticleDto from(Article article, boolean viewedByMe) {
        return new ArticleDto(
            article.getId(),
            article.getSource().name(),
            article.getSourceUrl(),
            article.getTitle(),
            article.getPublishDate(),
            article.getCommentCount(),
            article.getViewCount(),
            viewedByMe
        );
    }
}