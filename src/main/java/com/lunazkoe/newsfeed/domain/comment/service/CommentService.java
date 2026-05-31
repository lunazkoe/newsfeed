package com.lunazkoe.newsfeed.domain.comment.service;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 목록 조회
     */

    /**
     * 댓글 등록
     */
    @Transactional
    public CommentDto register(CommentRegisterRequest request) {
        Article foundArticle = articleRepository.findById(request.articleId())
            .orElseThrow(() -> new ArticleException(ArticleErrorCode.ARTICLE_NOT_FOUND));
        User foundUser = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 댓글 생성
        Comment newComment = Comment.create(foundArticle, foundUser, request.content());
        commentRepository.save(newComment);

        // 기사 댓글 수 증가
        foundArticle.increaseCommentCount();

        log.info("댓글 등록 요청 성공. CommentId: {}", newComment.getId());
        return CommentDto.from(newComment, false);
    }

    /**
     * 댓글 논리 삭제
     */

    /**
     * 댓글 정보 수정
     */

    /**
     * 댓글 물리 삭제
     */

}
