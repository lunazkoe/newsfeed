package com.lunazkoe.newsfeed.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.notification.dto.NotificationDto;
import com.lunazkoe.newsfeed.domain.notification.dto.NotificationSearchCondition;
import com.lunazkoe.newsfeed.domain.notification.entity.Notification;
import com.lunazkoe.newsfeed.domain.notification.entity.NotificationResourceType;
import com.lunazkoe.newsfeed.domain.notification.exception.NotificationErrorCode;
import com.lunazkoe.newsfeed.domain.notification.exception.NotificationException;
import com.lunazkoe.newsfeed.domain.notification.listener.CreateNotificationEvent;
import com.lunazkoe.newsfeed.domain.notification.repository.NotificationRepository;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Notification notification;
    private UUID userId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        user = User.create("test@email.com", "테스터", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        // 엔티티 생성 (파라미터는 실제 구조에 맞게 조정 필요)
        notification = Notification.create(user, "테스트 알림입니다.", NotificationResourceType.COMMENT, UUID.randomUUID());
        ReflectionTestUtils.setField(notification, "id", notificationId);
        ReflectionTestUtils.setField(notification, "confirmed", false);
    }

    @Nested
    @DisplayName("알림 목록 조회(getNotifications) 테스트")
    class GetNotificationsTest {

        @Test
        @DisplayName("성공: 조건에 맞는 알림 목록을 조회하고 DTO로 변환하여 반환한다.")
        void getNotifications_success() {
            // given
            NotificationSearchCondition condition = new NotificationSearchCondition(null, null, 10);

            LocalDateTime createdAt = LocalDateTime.now();

            // 레포지토리가 반환할 가짜(Mock) 응답 객체 생성
            List<Notification> notifications = List.of(notification);
            CursorPageResponse<Notification> mockPageResponse = new CursorPageResponse<>(
                notifications,
                createdAt.toString(), // nextCursor
                createdAt, // nextAfter
                1, // size
                1L, // totalElements
                false // hasNext
            );

            given(notificationRepository.searchNotifications(condition, userId))
                .willReturn(mockPageResponse);

            // when
            CursorPageResponse<NotificationDto> result = notificationService.getNotifications(condition, userId);

            // then
            // 1. 레포지토리 메서드가 제대로 호출되었는지 검증
            verify(notificationRepository).searchNotifications(condition, userId);

            // 2. DTO 변환 및 값 매핑이 정상적으로 이루어졌는지 검증
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).id()).isEqualTo(notification.getId());
            assertThat(result.content().get(0).content()).isEqualTo(notification.getContent());

            // 3. 페이지네이션 메타데이터가 그대로 잘 전달되었는지 검증
            assertThat(result.nextCursor()).isEqualTo(mockPageResponse.nextCursor());
            assertThat(result.totalElements()).isEqualTo(mockPageResponse.totalElements());
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("전체 알림 확인(confirmAllNotification) 테스트")
    class ConfirmAllNotificationTest {

        @Test
        @DisplayName("성공: 유저가 존재하면 벌크 업데이트 쿼리가 실행된다.")
        void confirmAll_success() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationRepository.confirmAllByUserId(eq(userId), any(LocalDateTime.class)))
                .willReturn(5); // 5건 업데이트 가정

            // when
            notificationService.confirmAllNotification(userId);

            // then
            verify(notificationRepository).confirmAllByUserId(eq(userId), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("실패: 유저가 존재하지 않으면 예외가 발생하고 업데이트는 실행되지 않는다.")
        void confirmAll_fail_userNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            UserException exception = assertThrows(UserException.class, () -> {
                notificationService.confirmAllNotification(userId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);

            verify(notificationRepository, never()).confirmAllByUserId(any(), any());
        }
    }

    @Nested
    @DisplayName("단건 알림 확인(confirmNotification) 테스트")
    class ConfirmNotificationTest {

        @Test
        @DisplayName("성공: 알림과 유저가 모두 존재하면 알림이 읽음(confirmed) 처리된다.")
        void confirm_success() {
            // given
            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            assertThat(notification.isConfirmed()).isFalse(); // 초기 상태 확인

            // when
            notificationService.confirmNotification(notificationId, userId);

            // then
            assertThat(notification.isConfirmed()).isTrue(); // 상태 변경 확인
        }

        @Test
        @DisplayName("실패: 알림이 존재하지 않으면 예외가 발생한다.")
        void confirm_fail_notificationNotFound() {
            // given
            given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

            // when & then
            NotificationException exception = assertThrows(NotificationException.class, () -> {
                notificationService.confirmNotification(notificationId, userId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);

            // 유저 조회 로직까지 넘어가지 않아야 함
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("실패: 알림은 존재하나 유저가 존재하지 않으면 예외가 발생한다.")
        void confirm_fail_userNotFound() {
            // given
            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            UserException exception = assertThrows(UserException.class, () -> {
                notificationService.confirmNotification(notificationId, userId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);

            // confirm() 메서드가 실행되어 상태가 변하면 안 됨 (트랜잭션 롤백 테스트 대용)
            assertThat(notification.isConfirmed()).isFalse();
        }
    }

    @Nested
    @DisplayName("알림 생성 이벤트 핸들러(createNotification) 테스트")
    class CreateNotificationTest {

        @Test
        @DisplayName("성공: 수신자가 존재하면 알림을 생성하고 저장한다.")
        void createNotification_success() {
            // given
            CreateNotificationEvent event = new CreateNotificationEvent(userId, "새로운 알림", NotificationResourceType.COMMENT, UUID.randomUUID());
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            notificationService.createNotification(event);

            // then
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("성공(무시): 수신자가 존재하지 않으면 예외를 던지지 않고 무시(return)한다.")
        void createNotification_ignore_userNotFound() {
            // given
            CreateNotificationEvent event = new CreateNotificationEvent(userId, "새로운 알림", NotificationResourceType.COMMENT, UUID.randomUUID());
            given(userRepository.findById(userId)).willReturn(Optional.empty()); // 유저 없음

            // when
            notificationService.createNotification(event);

            // then
            // 핵심 검증: save 로직이 절대 실행되면 안 됨
            verify(notificationRepository, never()).save(any(Notification.class));
        }
    }
}