package com.lunazkoe.newsfeed.domain.article.exception;

import com.lunazkoe.newsfeed.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ArticleErrorCode implements ErrorCode {
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ARTICLE_NOT_FOUND", "해당 뉴스 기사 정보는 없습니다.");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
