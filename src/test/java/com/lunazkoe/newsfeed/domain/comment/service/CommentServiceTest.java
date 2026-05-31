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
import com.lunazkoe.newsfeed.domain.comment.dto.CommentUpdateRequest;
import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import com.lunazkoe.newsfeed.domain.comment.exception.CommentErrorCode;
import com.lunazkoe.newsfeed.domain.comment.exception.CommentException;
import com.lunazkoe.newsfeed.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsfeed.domain.commentlike.repository.CommentLikeRepository;
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
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;

    private User user;
    private Article article;
    private Comment comment;
    private UUID userId;
    private UUID articleId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        articleId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        user = User.create("test@email.com", "테스터", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        article = Article.create(ArticleSource.NAVER, "https://test.url", "테스트 기사", "요약", LocalDateTime.now());
        ReflectionTestUtils.setField(article, "id", articleId);
        ReflectionTestUtils.setField(article, "commentCount", 0L);

        comment = Comment.create(article, user, "기존 댓글 내용");
        ReflectionTestUtils.setField(comment, "id", commentId);
    }

    @Nested
    @DisplayName("댓글 등록(register) 테스트")
    class RegisterTest {

        @Test
        @DisplayName("성공: 유효한 요청 시 댓글이 생성되고 기사의 댓글 수가 1 증가한다.")
        void register_success() {
            // given
            CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "새로운 댓글 내용");
            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            assertThat(article.getCommentCount()).isEqualTo(0L);

            // when
            CommentDto result = commentService.register(request);

            // then
            assertThat(result.content()).isEqualTo("새로운 댓글 내용");
            verify(commentRepository).save(any(Comment.class));
            assertThat(article.getCommentCount()).isEqualTo(1L); // 기사 댓글 수 증가 검증
        }

        @Test
        @DisplayName("실패: 기사가 존재하지 않으면 예외가 발생한다.")
        void register_fail_articleNotFound() {
            // given
            CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "새로운 댓글 내용");
            given(articleRepository.findById(articleId)).willReturn(Optional.empty());

            // when & then
            ArticleException exception = assertThrows(ArticleException.class, () -> {
                commentService.register(request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ArticleErrorCode.ARTICLE_NOT_FOUND);
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("댓글 정보 수정(updateContent) 테스트")
    class UpdateContentTest {

        @Test
        @DisplayName("성공: 댓글 내용이 수정되고 좋아요 여부(likedByMe)가 정확히 반환된다.")
        void updateContent_success() {
            // given
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글 내용");
            given(commentRepository.findWithUserById(commentId)).willReturn(Optional.of(comment));
            given(commentLikeRepository.existsByCommentIdAndUserIdDirectly(userId, commentId)).willReturn(true);

            // when
            CommentDto result = commentService.updateContent(commentId, userId, request);

            // then
            assertThat(result.id()).isEqualTo(commentId);
            assertThat(result.content()).isEqualTo("수정된 댓글 내용"); // 엔티티 내용 수정 확인
            assertThat(result.likedByMe()).isTrue();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 댓글 수정 요청 시 예외가 발생한다.")
        void updateContent_fail_commentNotFound() {
            // given
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글 내용");
            given(commentRepository.findWithUserById(commentId)).willReturn(Optional.empty());

            // when & then
            CommentException exception = assertThrows(CommentException.class, () -> {
                commentService.updateContent(commentId, userId, request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);

            // exists 쿼리가 나가지 않아야 함
            verify(commentLikeRepository, never()).existsByCommentIdAndUserIdDirectly(any(), any());
        }
    }
}