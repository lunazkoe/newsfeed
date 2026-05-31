package com.lunazkoe.newsfeed.domain.notification.controller;

import static com.lunazkoe.newsfeed.global.filter.MDCLoggingFilter.HEADER_USER_ID;

import com.lunazkoe.newsfeed.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "전체 알림 확인", description = "전체 알림을 한번에 확인합니다.")
    @PatchMapping()
    public ResponseEntity<Void> confirmAllNotifications(@RequestHeader(HEADER_USER_ID) UUID requestUserId) {
        notificationService.confirmAllNotification(requestUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }

    @Operation(summary = "알림 확인", description = "알림을 확인합니다.")
    @PatchMapping("/{notificationId}")
    public ResponseEntity<Void> confirmNotification(
        @PathVariable UUID notificationId,
        @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        notificationService.confirmNotification(notificationId, requestUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }
}
