package com.lunazkoe.newsfeed.domain.article.entity;

import static com.lunazkoe.newsfeed.global.entity.BaseSoftDeleteEntity.IS_DELETED_FALSE_ONLY;

import com.lunazkoe.newsfeed.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(IS_DELETED_FALSE_ONLY)
public class Article extends BaseSoftDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "article_id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleSource source;

    @Column(nullable = false, length = 2048)
    private String sourceUrl;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
//    @Lob
    private String summary;

    @Column(nullable = false)
    private LocalDateTime publishDate;

    @Column(nullable = false)
    private long commentCount = 0L;

    @Column(nullable = false)
    private long viewCount = 0L;

    private Article(ArticleSource source, String sourceUrl, String title, String summary, LocalDateTime publishDate) {
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.summary = summary;
        this.publishDate = publishDate;
    }

    // == 생성 메서드 ==
    public static Article create(ArticleSource source, String sourceUrl, String title, String summary, LocalDateTime publishDate) {
        return new Article(source, sourceUrl, title, summary, publishDate);
    }

    // == 비즈니스 메서드 ==
    public void increaseCommentCount() {
        if (this.commentCount < Long.MAX_VALUE) {
            commentCount++;
        }
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            commentCount--;
        }
    }

    public void increateViewCount() {
        if (this.viewCount < Long.MAX_VALUE) {
            viewCount++;
        }
    }

}
