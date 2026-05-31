package com.lunazkoe.newsfeed.domain.commentlike.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.lunazkoe.newsfeed.domain.notification.listener.CreateNotificationEvent;
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
import org.springframework.context.ApplicationEventPublisher;
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
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User author;
    private User liker;
    private Article article;
    private Comment comment;
    private CommentLike commentLike;
    private UUID authorId;
    private UUID likerId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        likerId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        // 작성자와 좋아요 누르는 사람을 분리
        author = User.create("author@email.com", "작성자", "password");
        ReflectionTestUtils.setField(author, "id", authorId);

        liker = User.create("liker@email.com", "좋아요누른사람", "password");
        ReflectionTestUtils.setField(liker, "id", likerId);

        article = Article.create(ArticleSource.NAVER, "url", "제목", "요약", LocalDateTime.now());

        comment = Comment.create(article, author, "댓글 내용");
        ReflectionTestUtils.setField(comment, "id", commentId);
        ReflectionTestUtils.setField(comment, "likeCount", 0L);

        commentLike = CommentLike.create(comment, liker);
    }

    @Nested
    @DisplayName("댓글 좋아요(likeComment) 테스트")
    class LikeCommentTest {

        @Test
        @DisplayName("성공: 좋아요 기록이 없으면 새롭게 좋아요를 등록하고 알림 이벤트를 발행한다.")
        void likeComment_success_newLikeAndEventPublished() {
            // given
            given(commentLikeRepository.findByCommentIdAndUserId(commentId, likerId)).willReturn(Optional.empty());
            given(commentRepository.findWithUserById(commentId)).willReturn(Optional.of(comment));
            given(userRepository.findById(likerId)).willReturn(Optional.of(liker));

            assertThat(comment.getLikeCount()).isEqualTo(0L);

            // when
            CommentLikeDto result = commentLikeService.likeComment(commentId, likerId);

            // then
            assertThat(result.commentId()).isEqualTo(commentId);
            verify(commentLikeRepository).save(any(CommentLike.class));
            assertThat(comment.getLikeCount()).isEqualTo(1L); // 좋아요 수 증가 검증

            // 핵심 검증: 남의 댓글이므로 알림 이벤트가 발행되어야 함
            verify(eventPublisher).publishEvent(any(CreateNotificationEvent.class));
        }

        @Test
        @DisplayName("성공: 내가 쓴 댓글에 좋아요를 누르면 알림 이벤트를 발행하지 않는다.")
        void likeComment_success_selfLikeNoEvent() {
            // given (likerId 대신 authorId가 좋아요를 요청)
            given(commentLikeRepository.findByCommentIdAndUserId(commentId, authorId)).willReturn(Optional.empty());
            given(commentRepository.findWithUserById(commentId)).willReturn(Optional.of(comment));
            given(userRepository.findById(authorId)).willReturn(Optional.of(author)); // 본인 정보 조회

            // when
            commentLikeService.likeComment(commentId, authorId);

            // then
            verify(commentLikeRepository).save(any(CommentLike.class));
            assertThat(comment.getLikeCount()).isEqualTo(1L);

            // 핵심 검증: 본인 댓글이므로 이벤트가 발행되지 않아야 함
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("성공: 이미 좋아요를 누른 상태라면 추가 조회나 저장 없이 기존 기록을 반환한다.")
        void likeComment_success_alreadyLiked() {
            // given
            given(commentLikeRepository.findByCommentIdAndUserId(commentId, likerId)).willReturn(Optional.of(commentLike));

            // when
            commentLikeService.likeComment(commentId, likerId);

            // then
            verify(commentRepository, never()).findWithUserById(any());
            verify(userRepository, never()).findById(any());
            verify(commentLikeRepository, never()).save(any(CommentLike.class));
            assertThat(comment.getLikeCount()).isEqualTo(0L); // 수량 변화 없음
        }

        @Test
        @DisplayName("실패: 댓글이 존재하지 않으면 예외가 발생한다.")
        void likeComment_fail_commentNotFound() {
            // given
            given(commentLikeRepository.findByCommentIdAndUserId(commentId, likerId)).willReturn(Optional.empty());
            given(commentRepository.findWithUserById(commentId)).willReturn(Optional.empty());

            // when & then
            CommentException exception = assertThrows(CommentException.class, () -> {
                commentLikeService.likeComment(commentId, likerId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 좋아요 취소(cancelLikeComment) 테스트")
    class CancelLikeCommentTest {

        @Test
        @DisplayName("성공: 좋아요 기록이 있다면 삭제하고 댓글의 좋아요 수를 1 감소시킨다.")
        void cancel_success() {
            // given
            comment.increaseLikeCount(); // 테스트용: 초기 카운트를 1로 셋팅
            assertThat(comment.getLikeCount()).isEqualTo(1L);

            given(commentLikeRepository.findByCommentIdAndUserIdWithComment(commentId, likerId))
                .willReturn(Optional.of(commentLike));

            // when
            commentLikeService.cancelLikeComment(commentId, likerId);

            // then
            verify(commentLikeRepository).delete(commentLike);
            assertThat(comment.getLikeCount()).isEqualTo(0L); // 카운트 0으로 감소 확인
        }

        @Test
        @DisplayName("성공(무시): 좋아요 기록이 없다면 아무 일도 일어나지 않는다.")
        void cancel_success_noRecord() {
            // given
            given(commentLikeRepository.findByCommentIdAndUserIdWithComment(commentId, likerId))
                .willReturn(Optional.empty());

            // when
            commentLikeService.cancelLikeComment(commentId, likerId);

            // then
            verify(commentLikeRepository, never()).delete(any());
        }
    }
}