package com.lunazkoe.newsfeed.domain.interest.exception;

import com.lunazkoe.newsfeed.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterestErrorCode implements ErrorCode {

    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST_NOT_FOUND", "관심사 정보를 찾을 수 없습니다."),
    SIMILAR_INTEREST_EXISTS(HttpStatus.CONFLICT, "SIMILAR_INTEREST_EXISTS", "80% 이상 유사한 관심사가 이미 존재합니다. 기존 관심사를 확인해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
