package com.lunazkoe.newsfeed.domain.commentlike.controller;

import static com.lunazkoe.newsfeed.global.filter.MDCLoggingFilter.*;

import com.lunazkoe.newsfeed.domain.commentlike.dto.CommentLikeDto;
import com.lunazkoe.newsfeed.domain.commentlike.service.CommentLikeService;
import com.lunazkoe.newsfeed.global.filter.MDCLoggingFilter;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private CommentLikeService commentLikeService;

    @Operation(summary = "관심사 댓글 좋아요", description = "댓글 좋아요를 등록합니다.")
    @PostMapping("/{commentId}/comment-likes")
    public ResponseEntity<CommentLikeDto> likeComment(@PathVariable UUID commentId, @RequestHeader(HEADER_USER_ID) UUID requestUserId) {
        CommentLikeDto response = commentLikeService.likeComment(commentId, requestUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
