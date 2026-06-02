package com.lunazkoe.newsfeed.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record CommentSearchCondition(
    @Schema(description = "기사 ID (특정 기사의 댓글만 조회할 경우)", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID articleId,

    @Schema(description = "정렬 속성 (createdAt, likeCount)", example = "createdAt")
    @Pattern(regexp = "^(createdAt|likeCount)$", message = "정렬 기준은 createdAt 또는 likeCount만 가능합니다.")
    String orderBy,

    @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC")
    @Pattern(regexp = "(?i)^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 가능합니다.")
    String direction,

    @Schema(description = "커서 값")
    String cursor,

    @Schema(description = "보조 커서 값 (마지막으로 조회된 데이터의 생성일시)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime after,

    @Schema(description = "커서 페이지 크기", example = "50")
    @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
    @Max(value = 100, message = "한 번에 조회할 수 있는 최대 개수는 100개입니다.")
    Integer limit
) {

}
