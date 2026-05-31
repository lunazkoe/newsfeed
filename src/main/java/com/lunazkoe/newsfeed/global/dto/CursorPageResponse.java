package com.lunazkoe.newsfeed.global.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    LocalDateTime nextAfter,
    int size,
    Long totalElements,
    Boolean hasNext
) {

}
