package com.lunazkoe.newsfeed.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleSource;
import com.lunazkoe.newsfeed.domain.article.exception.ArticleErrorCode;
import com.lunazkoe.newsfeed.domain.article.exception.ArticleException;
import com.lunazkoe.newsfeed.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsfeed.domain.comment.dto.CommentDto;
import com.lunazkoe.newsfeed.domain.comment.dto.CommentRegisterRequest;
import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import com.lunazkoe.newsfeed.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
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
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Article article;
    private UUID userId;
    private UUID articleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        articleId = UUID.randomUUID();

        // 사용자 세팅
        user = User.create("test@email.com", "닉네임", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        // 기사 세팅
        article = Article.create(ArticleSource.NAVER, "url", "제목", "요약", LocalDateTime.now());
        ReflectionTestUtils.setField(article, "id", articleId);
    }

    @Nested
    @DisplayName("댓글 등록(register) 테스트")
    class RegisterTest {

        @Test
        @DisplayName("성공: 존재하는 기사와 사용자 ID로 요청 시 댓글이 등록되고 기사의 댓글 수가 1 증가한다.")
        void register_success() {
            // given
            CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "새로운 댓글입니다.");

            given(articleRepository.findById(request.articleId())).willReturn(Optional.of(article));
            given(userRepository.findById(request.userId())).willReturn(Optional.of(user));

            long initialCommentCount = article.getCommentCount();

            // when
            CommentDto result = commentService.register(request);

            // then
            assertThat(result.articleId()).isEqualTo(articleId);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.userNickname()).isEqualTo("닉네임");
            assertThat(result.content()).isEqualTo("새로운 댓글입니다.");
            assertThat(result.likedByMe()).isFalse(); // 기본값 false 확인

            // 기사의 댓글 수가 증가했는지 검증
            assertThat(article.getCommentCount()).isEqualTo(initialCommentCount + 1);

            verify(articleRepository).findById(request.articleId());
            verify(userRepository).findById(request.userId());
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 기사 ID로 요청 시 예외가 발생한다.")
        void register_fail_articleNotFound() {
            // given
            CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "새로운 댓글입니다.");

            given(articleRepository.findById(request.articleId())).willReturn(Optional.empty());

            // when & then
            ArticleException exception = assertThrows(ArticleException.class, () -> {
                commentService.register(request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ArticleErrorCode.ARTICLE_NOT_FOUND);

            verify(articleRepository).findById(request.articleId());
            verify(userRepository, never()).findById(any(UUID.class));
            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID로 요청 시 예외가 발생한다.")
        void register_fail_userNotFound() {
            // given
            CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "새로운 댓글입니다.");

            given(articleRepository.findById(request.articleId())).willReturn(Optional.of(article));
            given(userRepository.findById(request.userId())).willReturn(Optional.empty());

            // when & then
            UserException exception = assertThrows(UserException.class, () -> {
                commentService.register(request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);

            verify(articleRepository).findById(request.articleId());
            verify(userRepository).findById(request.userId());
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }
}