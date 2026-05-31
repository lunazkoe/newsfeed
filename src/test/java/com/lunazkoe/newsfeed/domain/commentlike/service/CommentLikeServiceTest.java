package com.lunazkoe.newsfeed.domain.commentlike.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleSource;
import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import com.lunazkoe.newsfeed.domain.comment.exception.CommentErrorCode;
import com.lunazkoe.newsfeed.domain.comment.exception.CommentException;
import com.lunazkoe.newsfeed.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsfeed.domain.commentlike.dto.CommentLikeDto;
import com.lunazkoe.newsfeed.domain.commentlike.entity.CommentLike;
import com.lunazkoe.newsfeed.domain.commentlike.repository.CommentLikeRepository;
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
class CommentLikeServiceTest {
    @InjectMocks
    private CommentLikeService commentLikeService;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    private User commentAuthor;
    private User requestUser;
    private Article article;
    private Comment comment;
    private UUID commentId;
    private UUID requestUserId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        requestUserId = UUID.randomUUID();

        // 1. 댓글 작성자 세팅
        commentAuthor = User.create("author@email.com", "작성자", "password");
        ReflectionTestUtils.setField(commentAuthor, "id", UUID.randomUUID());

        // 2. 좋아요 요청자 세팅
        requestUser = User.create("liker@email.com", "좋아요누른사람", "password");
        ReflectionTestUtils.setField(requestUser, "id", requestUserId);

        // 3. 기사 세팅
        article = Article.create(ArticleSource.NAVER, "url", "제목", "요약", LocalDateTime.now());
        ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

        // 4. 댓글 세팅
        comment = Comment.create(article, commentAuthor, "댓글내용입니다.");
        ReflectionTestUtils.setField(comment, "id", commentId);
    }

    @Nested
    @DisplayName("댓글 좋아요(likeComment) 테스트")
    class LikeCommentTest {

        @Test
        @DisplayName("성공: 좋아요를 처음 누르는 경우 새로운 좋아요가 저장되고 좋아요 수가 증가한다.")
        void likeComment_success_firstTime() {
            // given
            given(commentLikeRepository.findByCommentIdAndUserId(commentId, requestUserId))
                .willReturn(Optional.empty());

            given(commentRepository.findWithUserById(commentId))
                .willReturn(Optional.of(comment));

            given(userRepository.findById(requestUserId))
                .willReturn(Optional.of(requestUser));

            long initialLikeCount = comment.getLikeCount();

            // when
            CommentLikeDto result = commentLikeService.likeComment(commentId, requestUserId);

            // then
            assertThat(result.commentId()).isEqualTo(commentId);
            assertThat(result.likedBy()).isEqualTo(requestUserId);
            assertThat(result.commentContent()).isEqualTo("댓글내용입니다.");
            assertThat(comment.getLikeCount()).isEqualTo(initialLikeCount + 1); // 좋아요 수 증가 확인

            verify(commentLikeRepository).findByCommentIdAndUserId(commentId, requestUserId);
            verify(commentRepository).findWithUserById(commentId);
            verify(userRepository).findById(requestUserId);
            verify(commentLikeRepository).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("성공: 이미 좋아요를 누른 경우 상태를 변경하지 않고 기존 좋아요 정보를 반환한다.")
        void likeComment_success_alreadyLiked() {
            // given
            CommentLike existingLike = CommentLike.create(comment, requestUser);
            ReflectionTestUtils.setField(existingLike, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(existingLike, "createdAt", LocalDateTime.now());

            given(commentLikeRepository.findByCommentIdAndUserId(commentId, requestUserId))
                .willReturn(Optional.of(existingLike));

            long initialLikeCount = comment.getLikeCount();

            // when
            CommentLikeDto result = commentLikeService.likeComment(commentId, requestUserId);

            // then
            assertThat(result.commentId()).isEqualTo(commentId);
            assertThat(result.likedBy()).isEqualTo(requestUserId);
            assertThat(comment.getLikeCount()).isEqualTo(initialLikeCount); // 좋아요 수 증가 안 됨 확인

            verify(commentLikeRepository).findByCommentIdAndUserId(commentId, requestUserId);

            // 캐싱된 좋아요를 반환하므로 DB 조회가 더 이상 발생하지 않아야 함
            verify(commentRepository, never()).findWithUserById(any(UUID.class));
            verify(userRepository, never()).findById(any(UUID.class));
            verify(commentLikeRepository, never()).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 댓글에 좋아요를 시도하면 예외가 발생한다.")
        void likeComment_fail_commentNotFound() {
            // given
            given(commentLikeRepository.findByCommentIdAndUserId(commentId, requestUserId))
                .willReturn(Optional.empty());

            given(commentRepository.findWithUserById(commentId))
                .willReturn(Optional.empty());

            // when & then
            CommentException exception = assertThrows(CommentException.class, () -> {
                commentLikeService.likeComment(commentId, requestUserId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);

            verify(userRepository, never()).findById(any(UUID.class));
            verify(commentLikeRepository, never()).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID로 좋아요를 시도하면 예외가 발생한다.")
        void likeComment_fail_userNotFound() {
            // given
            given(commentLikeRepository.findByCommentIdAndUserId(commentId, requestUserId))
                .willReturn(Optional.empty());

            given(commentRepository.findWithUserById(commentId))
                .willReturn(Optional.of(comment));

            given(userRepository.findById(requestUserId))
                .willReturn(Optional.empty());

            // when & then
            UserException exception = assertThrows(UserException.class, () -> {
                commentLikeService.likeComment(commentId, requestUserId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);

            verify(commentLikeRepository, never()).save(any(CommentLike.class));
        }
    }
}