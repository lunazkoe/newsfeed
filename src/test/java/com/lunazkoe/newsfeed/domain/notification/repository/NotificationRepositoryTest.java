package com.lunazkoe.newsfeed.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lunazkoe.newsfeed.domain.notification.dto.NotificationSearchCondition;
import com.lunazkoe.newsfeed.domain.notification.entity.Notification;
import com.lunazkoe.newsfeed.domain.notification.entity.NotificationResourceType;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import com.lunazkoe.newsfeed.global.config.JpaAuditingConfig;
import com.lunazkoe.newsfeed.global.config.QuerydslConfig;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class}) // Querydsl의 JPAQueryFactory를 빈으로 등록하기 위한 설정 주입
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("커서 기반 알림 조회: 첫 페이지 조회 시 총 개수와 limit 개수만큼 알림을 가져온다.")
    void searchNotifications_firstPage() {
        // given
        User user = userRepository.save(User.create("test@test.com", "테스터", "1234"));

        // 테스트용 알림 데이터 15개 저장
        for (int i = 0; i < 15; i++) {
            notificationRepository.save(Notification.create(user, "알림 " + i, NotificationResourceType.COMMENT, UUID.randomUUID()));
        }

        // condition: cursor null, after null, limit 10 (첫 페이지 요청)
        NotificationSearchCondition condition = new NotificationSearchCondition(null, null, 10);

        // when
        CursorPageResponse<Notification> response = notificationRepository.searchNotifications(condition, user.getId());

        // then
        assertThat(response.content()).hasSize(10); // limit 개수만큼 짤라서 반환해야 함
        assertThat(response.hasNext()).isTrue();    // 15개 중 10개를 가져왔으니 다음 페이지가 있어야 함
        assertThat(response.totalElements()).isEqualTo(15L); // 첫 페이지이므로 전체 개수가 계산되어야 함
        assertThat(response.nextCursor()).isNotNull(); // 다음 조회를 위한 커서가 세팅되어야 함
    }
}