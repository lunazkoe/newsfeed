package com.lunazkoe.newsfeed.domain.comment.controller;

import static com.lunazkoe.newsfeed.global.filter.MDCLoggingFilter.HEADER_USER_ID;

import com.lunazkoe.newsfeed.domain.comment.dto.CommentDto;
import com.lunazkoe.newsfeed.domain.comment.dto.CommentRegisterRequest;
import com.lunazkoe.newsfeed.domain.comment.dto.CommentUpdateRequest;
import com.lunazkoe.newsfeed.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
    @PostMapping()
    public ResponseEntity<CommentDto> register(@Valid @RequestBody CommentRegisterRequest request) {
        log.info("댓글 등록 요청 수신");
        CommentDto response = commentService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @Operation(summary = "댓글 정보 수정", description = "댓글의 내용을 수정합니다.")
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateCommentContent(
        @PathVariable UUID commentId,
        @RequestHeader(HEADER_USER_ID) UUID requestUserId,
        @Valid @RequestBody CommentUpdateRequest request
    ) {
        CommentDto response = commentService.updateContent(commentId, requestUserId, request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
