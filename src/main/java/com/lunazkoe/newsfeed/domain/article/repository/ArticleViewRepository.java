package com.lunazkoe.newsfeed.domain.article.repository;

import com.lunazkoe.newsfeed.domain.article.entity.ArticleView;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

    @Query("SELECT v FROM ArticleView v JOIN FETCH v.article WHERE v.article.id = :articleId AND v.user.id = :userId")
    Optional<ArticleView> findByArticleIdAndUserIdWithArticle(@Param("articleId") UUID articleId, @Param("userId") UUID userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM article_views WHERE article_id = :articleId AND user_id = :userId)", nativeQuery = true)
    boolean existsByArticleIdAndUserIdDirectly(@Param("articleId") UUID articleId, @Param("userId") UUID userId);
}
