package com.lunazkoe.newsfeed.domain.article.service;

import com.lunazkoe.newsfeed.domain.article.dto.ArticleDto;
import com.lunazkoe.newsfeed.domain.article.dto.ArticleViewDto;
import com.lunazkoe.newsfeed.domain.article.entity.Article;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleView;
import com.lunazkoe.newsfeed.domain.article.exception.ArticleErrorCode;
import com.lunazkoe.newsfeed.domain.article.exception.ArticleException;
import com.lunazkoe.newsfeed.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsfeed.domain.article.repository.ArticleViewRepository;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final UserRepository userRepository;

    /**
     * 기사 뷰 등록
     */
    @Transactional
    public ArticleViewDto registerView(UUID articleId, UUID requestUserId) {

        // 이미 조회를 했다면 그냥 원래 조회수 정보를 반환하고 종료
        Optional<ArticleView> foundArticleView = articleViewRepository.findByArticleIdAndUserIdWithArticle(
            articleId, requestUserId);
        if (foundArticleView.isPresent()) {
            return ArticleViewDto.from(foundArticleView.get());
        }

        Article foundArticle = articleRepository.findById(articleId)
            .orElseThrow(() -> new ArticleException(ArticleErrorCode.ARTICLE_NOT_FOUND));
        User proxyUser = userRepository.getReferenceById(requestUserId);

        // 조회 생성
        ArticleView newArticleView = ArticleView.create(foundArticle, proxyUser);
        articleViewRepository.save(newArticleView);

        // 조회수 증가
        foundArticle.increateViewCount();

        log.info("기사 조회 성공. ArticleId: {}", foundArticle.getId());
        return ArticleViewDto.from(newArticleView);
    }

    /**
     * 뉴스 기사 목록 조회
     */

    /**
     * 뉴스 기사 단건 조회
     */
    public ArticleDto getArticle(UUID articleId, UUID requestUserId) {
        Article foundArticle = articleRepository.findById(articleId)
            .orElseThrow(() -> new ArticleException(ArticleErrorCode.ARTICLE_NOT_FOUND));

        boolean viewedByMe = articleViewRepository.existsByArticleIdAndUserIdDirectly(articleId,
            requestUserId);

        log.info("뉴스 기사 단건 조회 성공. ArticleId: {}", foundArticle.getId());
        return ArticleDto.from(foundArticle, viewedByMe);
    }

    /**
     * 뉴스 기사 논리 삭제
     */

    /**
     * 뉴스 복구
     */

    /**
     * 뉴스 기사 물리 삭제
     */
}
