package com.lunazkoe.newsfeed.domain.notification.service;

import com.lunazkoe.newsfeed.domain.notification.entity.Notification;
import com.lunazkoe.newsfeed.domain.notification.exception.NotificationErrorCode;
import com.lunazkoe.newsfeed.domain.notification.exception.NotificationException;
import com.lunazkoe.newsfeed.domain.notification.listener.CreateNotificationEvent;
import com.lunazkoe.newsfeed.domain.notification.repository.NotificationRepository;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 목록 조회
     */

    /**
     * 전체 알림 확인
     */
    @Transactional
    public void confirmAllNotification(UUID requestUserId) {
        userRepository.findById(requestUserId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        int rowCount = notificationRepository.confirmAllByUserId(requestUserId, LocalDateTime.now());
        log.info("전체 알림 {}건 확인 요청 완료. UserId: {}", rowCount, requestUserId);
    }

    /**
     * 알림 확인
     */
    @Transactional
    public void confirmNotification(UUID notificationId, UUID requestUserId) {
        Notification foundNotification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        userRepository.findById(requestUserId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        foundNotification.confirm();

        log.info("알림 확인 요청 완료. UserId: {}", requestUserId);
    }

    /**
     * 알림 이벤트 핸들러 처리 (알림 생성)
     */
    @Transactional
    public void createNotification(CreateNotificationEvent event) {
        // 수신자 검증: 회원이 탈퇴했거나 없는 경우 알림 생성 무시
        User receiver = userRepository.findById(event.receiverId())
            .orElse(null);

        if (receiver == null) {
            log.warn("[Notification] 수신자를 찾을 수 없거나 탈퇴한 회원입니다. receiverId: {}", event.receiverId());
            return;
        }

        // 알림 생성
        Notification newNotification = Notification.create(receiver, event.content(),
            event.resourceType(), event.resourceId());

        notificationRepository.save(newNotification);
        log.info("[Notification Created] 알림 저장 완료. notificationId: {}, receiverId: {}",
            newNotification.getId(), newNotification.getUser().getId());
    }
}
