package com.lunazkoe.newsfeed.domain.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record NotificationSearchCondition(
    UUID cursor,

    // 쿼리 파라미터로 들어오는 날짜 문자열을 자동 파싱 (예: 2026-06-01T12:49:14)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime after,

    @NotNull(message = "limit 값은 필수입니다.")
    @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
    @Max(value = 100, message = "limit은 최대 100까지 가능합니다.")
    Integer limit
) {
}
