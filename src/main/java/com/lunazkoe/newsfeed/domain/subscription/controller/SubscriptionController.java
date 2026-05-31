package com.lunazkoe.newsfeed.domain.subscription.controller;

import static com.lunazkoe.newsfeed.global.filter.MDCLoggingFilter.HEADER_USER_ID;

import com.lunazkoe.newsfeed.domain.subscription.dto.SubscriptionDto;
import com.lunazkoe.newsfeed.domain.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "관심사 구독", description = "관심사를 구독합니다.")
    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionDto> subscribe(
        @PathVariable UUID interestId,
        @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        SubscriptionDto response = subscriptionService.registerSubscribeInterest(interestId, requestUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @Operation(summary = "관심사 구독 취소", description = "관심사 구독을 취소합니다.")
    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> cancelSubscription(
        @PathVariable UUID interestId,
        @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        subscriptionService.cancelSubscribeInterest(interestId, requestUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }
}
