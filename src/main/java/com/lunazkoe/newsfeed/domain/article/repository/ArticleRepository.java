package com.lunazkoe.newsfeed.domain.article.repository;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

}
