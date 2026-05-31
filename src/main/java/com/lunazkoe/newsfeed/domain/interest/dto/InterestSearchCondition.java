package com.lunazkoe.newsfeed.domain.interest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

@Schema(description = "관심사 목록 조회용")
public record InterestSearchCondition(
    @Schema(description = "검색어(관심사 이름)", example = "스포츠")
    String keyword,

    @Schema(description = "정렬 속성 (name, subscriberCount)", example = "subscriberCount")
    @Pattern(regexp = "^(name|subscriberCount)$", message = "정렬 기준은 name 또는 subscriberCount만 가능합니다.")
    String orderBy,

    @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC")
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 가능합니다.")
    String direction,

    @Schema(description = "커서 값 (마지막으로 조회된 Interest ID)")
    UUID cursor,

    @Schema(description = "보조 커서 값 (마지막으로 조회된 데이터의 생성일시)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime after,

    @Schema(description = "커서 페이지 크기", example = "50")
    @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
    @Max(value = 100, message = "한 번에 조회할 수 있는 최대 개수는 100개입니다.")
    Integer limit
) {
    // 아무 값도 안 들어왔을 때를 대비한 기본값 세팅
    public InterestSearchCondition {
        if (!StringUtils.hasText(orderBy)) orderBy = "subscriberCount";
        if (!StringUtils.hasText(direction)) direction = "DESC";
        direction = direction.toUpperCase();
        if (limit == null) limit = 50;
    }
}