package com.lunazkoe.newsfeed.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[CustomException] Code: {}, Message: {}, ExceptionType: {}", errorCode.getCode(), errorCode.getMessage(), e.getClass().getSimpleName());
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ErrorResponse.of(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnhandledException] Message: {}, ExceptionType: {}", e.getMessage(), e.getClass().getSimpleName(), e);
        return ResponseEntity
            .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR, e));
    }
}
