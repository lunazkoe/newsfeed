package com.lunazkoe.newsfeed.domain.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.article.dto.ArticleDto;
import com.lunazkoe.newsfeed.domain.article.dto.ArticleViewDto;
import com.lunazkoe.newsfeed.domain.article.entity.Article;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleSource;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleView;
import com.lunazkoe.newsfeed.domain.article.exception.ArticleErrorCode;
import com.lunazkoe.newsfeed.domain.article.exception.ArticleException;
import com.lunazkoe.newsfeed.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsfeed.domain.article.repository.ArticleViewRepository;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    private ArticleService articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Article article;
    private ArticleView articleView;
    private UUID userId;
    private UUID articleId;
    private UUID articleViewId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        articleId = UUID.randomUUID();
        articleViewId = UUID.randomUUID();

        user = User.create("test@email.com", "테스터", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        // Article 생성 (Enum이나 Source 등 생성자 파라미터는 실제 구조에 맞게 세팅 필요)
        // 아래는 임시 구조(Source.NAVER 등을 가정)로 작성했습니다.
        article = Article.create(ArticleSource.NAVER, "https://test.url", "테스트 기사 제목", "요약 내용", LocalDateTime.now());
        ReflectionTestUtils.setField(article, "id", articleId);
        ReflectionTestUtils.setField(article, "viewCount", 0L);

        articleView = ArticleView.create(article, user);
        ReflectionTestUtils.setField(articleView, "id", articleViewId);
        ReflectionTestUtils.setField(articleView, "createdAt", LocalDateTime.now());
    }

    @Nested
    @DisplayName("기사 뷰 등록(registerView) 테스트")
    class RegisterViewTest {

        @Test
        @DisplayName("성공: 조회 기록이 없으면 새로운 뷰를 생성하고 조회수를 증가시킨다.")
        void registerView_success_newView() {
            // given
            given(articleViewRepository.findByArticleIdAndUserIdWithArticle(articleId, userId))
                .willReturn(Optional.empty());
            given(articleRepository.findById(articleId))
                .willReturn(Optional.of(article));
            given(userRepository.getReferenceById(userId))
                .willReturn(user);

            assertThat(article.getViewCount()).isEqualTo(0L);

            // when
            ArticleViewDto result = articleService.registerView(articleId, userId);

            // then
            assertThat(result.articleId()).isEqualTo(articleId);
            verify(articleViewRepository).save(any(ArticleView.class));
            // 💡 오타 수정 반영: increateViewCount -> increaseViewCount 가정
            assertThat(article.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공: 이미 조회한 기록이 있으면 추가 DB 접근 없이 기존 정보를 반환한다.")
        void registerView_success_alreadyViewed() {
            // given
            given(articleViewRepository.findByArticleIdAndUserIdWithArticle(articleId, userId))
                .willReturn(Optional.of(articleView));

            // when
            ArticleViewDto result = articleService.registerView(articleId, userId);

            // then
            assertThat(result.articleId()).isEqualTo(articleId);

            // 핵심 검증: 기사 단건 조회, 프록시 유저 조회, 저장 로직이 전혀 수행되지 않아야 함
            verify(articleRepository, never()).findById(any());
            verify(userRepository, never()).getReferenceById(any());
            verify(articleViewRepository, never()).save(any(ArticleView.class));
            assertThat(article.getViewCount()).isEqualTo(0L); // 카운트 변동 없음
        }

        @Test
        @DisplayName("실패: 존재하지 않는 기사를 조회하려 하면 예외가 발생한다.")
        void registerView_fail_articleNotFound() {
            // given
            given(articleViewRepository.findByArticleIdAndUserIdWithArticle(articleId, userId))
                .willReturn(Optional.empty());
            given(articleRepository.findById(articleId))
                .willReturn(Optional.empty());

            // when & then
            ArticleException exception = assertThrows(ArticleException.class, () -> {
                articleService.registerView(articleId, userId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ArticleErrorCode.ARTICLE_NOT_FOUND);

            verify(articleViewRepository, never()).save(any(ArticleView.class));
        }
    }

    @Nested
    @DisplayName("기사 단건 조회(getArticle) 테스트")
    class GetArticleTest {

        @Test
        @DisplayName("성공: 내가 조회한 적 있는 기사를 성공적으로 불러온다 (viewedByMe = true).")
        void getArticle_success_viewedByMeTrue() {
            // given
            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
            // JPA 파생 쿼리 사용으로 메서드명이 변경되었다고 가정 (existsByArticleIdAndUserId)
            given(articleViewRepository.existsByArticleIdAndUserIdDirectly(articleId, userId))
                .willReturn(true);

            // when
            ArticleDto result = articleService.getArticle(articleId, userId);

            // then
            assertThat(result.id()).isEqualTo(articleId);
            assertThat(result.title()).isEqualTo("테스트 기사 제목");
            assertThat(result.viewedByMe()).isTrue();
        }

        @Test
        @DisplayName("성공: 내가 조회한 적 없는 기사를 성공적으로 불러온다 (viewedByMe = false).")
        void getArticle_success_viewedByMeFalse() {
            // given
            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
            given(articleViewRepository.existsByArticleIdAndUserIdDirectly(articleId, userId))
                .willReturn(false);

            // when
            ArticleDto result = articleService.getArticle(articleId, userId);

            // then
            assertThat(result.id()).isEqualTo(articleId);
            assertThat(result.viewedByMe()).isFalse();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 기사를 조회하려 하면 예외가 발생한다.")
        void getArticle_fail_articleNotFound() {
            // given
            given(articleRepository.findById(articleId)).willReturn(Optional.empty());

            // when & then
            ArticleException exception = assertThrows(ArticleException.class, () -> {
                articleService.getArticle(articleId, userId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ArticleErrorCode.ARTICLE_NOT_FOUND);

            // exists 쿼리가 나가지 않아야 함
            verify(articleViewRepository, never()).existsByArticleIdAndUserIdDirectly(any(), any());
        }
    }
}