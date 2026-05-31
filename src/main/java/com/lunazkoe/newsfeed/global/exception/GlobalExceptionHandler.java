package com.lunazkoe.newsfeed.global.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    // @Valid 검증 실패 예외 (@ModelAttribute / @RequestBody)
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.warn("[BindException] Message: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("validationErrors", fieldErrors);

        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            GlobalErrorCode.BAD_REQUEST.getHttpStatus().value(),
            GlobalErrorCode.BAD_REQUEST.getCode(),
            "입력값이 올바르지 않습니다.",
            details,
            e.getClass().getSimpleName()
        );

        return ResponseEntity
            .status(GlobalErrorCode.BAD_REQUEST.getHttpStatus())
            .body(response);
    }

    // 쿼리 파라미터 / 경로 변수 파싱 에러 (타입 불일치 상세 포함)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("[TypeMismatchException] 파라미터: {}, 잘못된 값: {}", e.getName(), e.getValue());

        Map<String, Object> details = new HashMap<>();
        details.put("invalidParameter", e.getName());
        details.put("invalidValue", e.getValue());
        details.put("expectedType", e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown");

        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            GlobalErrorCode.BAD_REQUEST.getHttpStatus().value(),
            GlobalErrorCode.BAD_REQUEST.getCode(),
            e.getName() + " 파라미터의 타입이 올바르지 않습니다.",
            details,
            e.getClass().getSimpleName()
        );

        return ResponseEntity
            .status(GlobalErrorCode.BAD_REQUEST.getHttpStatus())
            .body(response);
    }

    // JSON 바디 파싱 에러 (잘못된 포맷)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("[HttpMessageNotReadableException] JSON 파싱 에러: {}", e.getMessage());

        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
            GlobalErrorCode.BAD_REQUEST.getHttpStatus().value(),
            GlobalErrorCode.BAD_REQUEST.getCode(),
            "요청 데이터(JSON)의 형식이 올바르지 않거나 파싱할 수 없습니다.",
            new HashMap<>(),
            e.getClass().getSimpleName()
        );

        return ResponseEntity
            .status(GlobalErrorCode.BAD_REQUEST.getHttpStatus())
            .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnhandledException] Message: {}, ExceptionType: {}", e.getMessage(), e.getClass().getSimpleName(), e);
        return ResponseEntity
            .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR, e));
    }
}
