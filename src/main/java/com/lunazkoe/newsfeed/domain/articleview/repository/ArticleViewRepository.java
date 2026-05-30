package com.lunazkoe.newsfeed.domain.articleview.repository;

import com.lunazkoe.newsfeed.domain.articleview.entity.ArticleView;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

}
