package com.lunazkoe.newsfeed.domain.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestErrorCode;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestException;
import com.lunazkoe.newsfeed.domain.interest.repository.InterestRepository;
import com.lunazkoe.newsfeed.domain.subscription.dto.SubscriptionDto;
import com.lunazkoe.newsfeed.domain.subscription.entity.Subscription;
import com.lunazkoe.newsfeed.domain.subscription.repository.SubscriptionRepository;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
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
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Interest interest;
    private Subscription subscription;
    private UUID userId;
    private UUID interestId;
    private UUID subscriptionId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        interestId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        // 임시 User 객체 (프록시 용도)
        user = User.create("test@email.com", "테스터", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        // 관심사 객체 생성
        interest = Interest.create("백엔드 개발", List.of("Java", "Spring"));
        ReflectionTestUtils.setField(interest, "id", interestId);

        // 구독 객체 생성
        subscription = Subscription.create(interest, user);
        ReflectionTestUtils.setField(subscription, "id", subscriptionId);
    }

    @Nested
    @DisplayName("관심사 구독(registerSubscribeInterest) 테스트")
    class RegisterSubscribeInterestTest {

        @Test
        @DisplayName("성공: 구독 중이 아니라면 새로운 구독을 생성하고 카운트를 증가시킨다.")
        void register_success_newSubscription() {
            // given
            given(subscriptionRepository.findByInterestIdAndUserIdWithInterestAndKeywords(interestId, userId))
                .willReturn(Optional.empty()); // 기존 구독 없음
            given(interestRepository.findByIdWithKeywords(interestId))
                .willReturn(Optional.of(interest)); // 관심사 정상 조회
            given(userRepository.getReferenceById(userId))
                .willReturn(user); // 프록시 유저 반환

            // 카운트 증가 확인을 위해 초기값 확인 (0이어야 함)
            assertThat(interest.getSubscriberCount()).isEqualTo(0L);

            // when
            SubscriptionDto result = subscriptionService.registerSubscribeInterest(interestId, userId);

            // then
            assertThat(result.interestId()).isEqualTo(interestId);
            assertThat(result.interestName()).isEqualTo("백엔드 개발");

            // 핵심 검증 1: 구독 엔티티가 정상적으로 save 호출되었는가?
            verify(subscriptionRepository).save(any(Subscription.class));
            // 핵심 검증 2: 관심사의 구독자 수가 1 증가했는가?
            assertThat(interest.getSubscriberCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공: 이미 구독 중이라면 추가 쿼리 없이 기존 구독 정보를 반환한다.")
        void register_success_alreadySubscribed() {
            // given
            given(subscriptionRepository.findByInterestIdAndUserIdWithInterestAndKeywords(interestId, userId))
                .willReturn(Optional.of(subscription)); // 이미 구독 중

            // when
            SubscriptionDto result = subscriptionService.registerSubscribeInterest(interestId, userId);

            // then
            assertThat(result.interestId()).isEqualTo(interestId);

            // 핵심 검증: 이미 구독 중이므로 save나 카운트 증가 로직이 절대 실행되면 안 됨!
            verify(interestRepository, never()).findByIdWithKeywords(any());
            verify(userRepository, never()).getReferenceById(any());
            verify(subscriptionRepository, never()).save(any(Subscription.class));
            assertThat(interest.getSubscriberCount()).isEqualTo(0L); // 카운트 변동 없음
        }

        @Test
        @DisplayName("실패: 존재하지 않는 관심사를 구독하려 하면 예외가 발생한다.")
        void register_fail_interestNotFound() {
            // given
            given(subscriptionRepository.findByInterestIdAndUserIdWithInterestAndKeywords(interestId, userId))
                .willReturn(Optional.empty());
            given(interestRepository.findByIdWithKeywords(interestId))
                .willReturn(Optional.empty()); // DB에 관심사가 없음

            // when & then
            InterestException exception = assertThrows(InterestException.class, () -> {
                subscriptionService.registerSubscribeInterest(interestId, userId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(InterestErrorCode.INTEREST_NOT_FOUND);

            verify(subscriptionRepository, never()).save(any(Subscription.class));
        }
    }

    @Nested
    @DisplayName("관심사 구독 취소(cancelSubscribeInterest) 테스트")
    class CancelSubscribeInterestTest {

        @Test
        @DisplayName("성공: 구독 중이라면 구독을 삭제하고 카운트를 1 감소시킨다.")
        void cancel_success() {
            // given
            interest.increaseSubscriberCount(); // 취소 테스트를 위해 카운트를 미리 1 올려둠
            assertThat(interest.getSubscriberCount()).isEqualTo(1L);

            given(subscriptionRepository.findByInterestIdAndUserIdWithInterest(interestId, userId))
                .willReturn(Optional.of(subscription));

            // when
            subscriptionService.cancelSubscribeInterest(interestId, userId);

            // then
            // 핵심 검증 1: delete가 정상적으로 호출되었는가?
            verify(subscriptionRepository).delete(subscription);
            // 핵심 검증 2: 카운트가 다시 0으로 감소했는가?
            assertThat(interest.getSubscriberCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("성공(무시): 구독 중이 아니라면 아무 일도 일어나지 않고 조용히 종료된다.")
        void cancel_success_notSubscribed() {
            // given
            given(subscriptionRepository.findByInterestIdAndUserIdWithInterest(interestId, userId))
                .willReturn(Optional.empty());

            // when
            subscriptionService.cancelSubscribeInterest(interestId, userId);

            // then
            // 핵심 검증: 찾지 못했으므로 삭제 로직이 실행되면 안 됨
            verify(subscriptionRepository, never()).delete(any(Subscription.class));
        }
    }
}