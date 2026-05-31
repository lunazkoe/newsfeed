package com.lunazkoe.newsfeed.domain.interest.dto;

import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import java.util.List;
import java.util.UUID;

public record InterestDto(
    UUID id,
    String name,
    List<String> keywords,
    Long subscriberCount,
    Boolean subscribedByMe
) {

    public static InterestDto from(Interest interest, Boolean subscribedByMe) {
        return new InterestDto(
            interest.getId(),
            interest.getName(),
            interest.getKeywords(),
            interest.getSubscriberCount(),
            subscribedByMe
        );
    }
}

