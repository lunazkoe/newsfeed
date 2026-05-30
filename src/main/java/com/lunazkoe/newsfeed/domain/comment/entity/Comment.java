package com.lunazkoe.newsfeed.domain.comment.entity;

import static com.lunazkoe.newsfeed.global.entity.BaseSoftDeleteEntity.IS_DELETED_FALSE_ONLY;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(IS_DELETED_FALSE_ONLY)
public class Comment extends BaseSoftDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comment_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private long likeCount = 0L;

    private Comment(Article article, User user, String content) {
        this.article = article;
        this.user = user;
        this.content = content;
    }

    // == 생성 메서드 ==
    public static Comment create(Article article, User user, String content) {
        return new Comment(article, user, content);
    }

    // == 비즈니스 메서드 ==
    /**
     * 댓글 내용 업데이트
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 댓글 좋아요 수 증가
     */
    public void increaseLikeCount() {
        if (this.likeCount < Long.MAX_VALUE) {
            this.likeCount++;
        }
    }

    /**
     * 댓글 좋아요 수 감소
     */
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}

/**
 * 단방향 1대다 매핑을 사용한 이유
 * 기사 / 사용자가 달 수 있는 댓글의 수는 정말 많음
 * 조회 시, 엄첨나게 많은 댓글을 가져와버리면 OOM이 발생할 수도 있음
 * 나중에 조회 시, Repository를 사용해서 가져오도록 처리하면 됨
 */
