package com.lunazkoe.newsfeed.domain.article.controller;

import static com.lunazkoe.newsfeed.global.filter.MDCLoggingFilter.HEADER_USER_ID;

import com.lunazkoe.newsfeed.domain.article.dto.ArticleDto;
import com.lunazkoe.newsfeed.domain.article.dto.ArticleViewDto;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleSource;
import com.lunazkoe.newsfeed.domain.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "기사 뷰 등록", description = "기사 뷰를 등록합니다.")
    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> registerView(
        @PathVariable UUID articleId,
        @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        ArticleViewDto response = articleService.registerView(articleId, requestUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @Operation(summary = "뉴스 기사 단건 조회", description = "뉴스 기사 ID로 뉴스 기사 단건을 조회합니다.")
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> getArticle(
        @PathVariable UUID articleId,
        @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        ArticleDto response = articleService.getArticle(articleId, requestUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @Operation(summary = "출처 목록 조회", description = "출처 목록을 조회합니다.")
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        List<String> response = Arrays.stream(ArticleSource.values())
            .map(Enum::name)
            .toList();
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
