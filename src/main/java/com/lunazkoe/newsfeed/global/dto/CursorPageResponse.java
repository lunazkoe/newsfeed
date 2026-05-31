package com.lunazkoe.newsfeed.global.dto;

import java.util.List;
import java.util.UUID;

public record CursorPageResponse<T>(
    List<T> content,
    UUID nextCursor,
    String nextAfter,
    int size,
    Long totalElements,
    Boolean hasNext
) {

}
