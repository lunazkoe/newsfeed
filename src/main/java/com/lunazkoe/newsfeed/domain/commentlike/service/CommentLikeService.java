package com.lunazkoe.newsfeed.domain.commentlike.service;

import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import com.lunazkoe.newsfeed.domain.comment.exception.CommentErrorCode;
import com.lunazkoe.newsfeed.domain.comment.exception.CommentException;
import com.lunazkoe.newsfeed.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsfeed.domain.commentlike.dto.CommentLikeDto;
import com.lunazkoe.newsfeed.domain.commentlike.entity.CommentLike;
import com.lunazkoe.newsfeed.domain.commentlike.repository.CommentLikeRepository;
import com.lunazkoe.newsfeed.domain.notification.listener.CreateNotificationEvent;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 관심사 댓글 좋아요
     */
    @Transactional
    public CommentLikeDto likeComment(UUID commentId, UUID requestUserId) {
        // 댓글 좋아요를 이미 했다면
        Optional<CommentLike> byCommentIdAndUserId = commentLikeRepository.findByCommentIdAndUserId(
            commentId, requestUserId);

        if (byCommentIdAndUserId.isPresent()) {
            return CommentLikeDto.from(byCommentIdAndUserId.get());
        }

        // 댓글 좋아요를 하지 않았다면 - **가져올 때 댓글 작성자도 함께 가져오기**
        Comment foundComment = commentRepository.findWithUserById(commentId)
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        User foundUser = userRepository.findById(requestUserId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        CommentLike newCommentLike = CommentLike.create(foundComment, foundUser);
        commentLikeRepository.save(newCommentLike);

        // 좋아요 수 증가
        foundComment.increaseLikeCount();

        // 좋아요 알림 생성
        // - 내가 쓴 댓글에 좋아요를 누를 경우 알림 생성 X
        if (!foundComment.getUser().getId().equals(requestUserId)) {
            eventPublisher.publishEvent(
                CreateNotificationEvent.createByCommentLike(foundComment, foundUser));
            log.info("[Event Published] NotificationCreateEvent for Comment Like. receiverId: {}, commentId: {}",
                foundComment.getUser().getId(), foundComment.getId());
        }

        log.info("관심사 댓글 좋아요 요청 성공. CommentId: {}", foundComment.getId());
        return CommentLikeDto.from(newCommentLike);
    }
}
