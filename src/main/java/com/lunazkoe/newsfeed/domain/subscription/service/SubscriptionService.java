package com.lunazkoe.newsfeed.domain.subscription.service;

import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestErrorCode;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestException;
import com.lunazkoe.newsfeed.domain.interest.repository.InterestRepository;
import com.lunazkoe.newsfeed.domain.subscription.dto.SubscriptionDto;
import com.lunazkoe.newsfeed.domain.subscription.entity.Subscription;
import com.lunazkoe.newsfeed.domain.subscription.repository.SubscriptionRepository;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InterestRepository interestRepository;
    private final UserRepository userRepository;

    /**
     * 관심사 구독
     */
    @Transactional
    public SubscriptionDto registerSubscribeInterest(UUID interestId, UUID requestUserId) {
        // 이미 구독 중인지 확인 (이미 구독 중인 경우 원래 구독 정보를 그냥 반환)
        Optional<Subscription> foundSubscription = subscriptionRepository.findByInterestIdAndUserIdWithInterestAndKeywords(
            interestId, requestUserId);
        if (foundSubscription.isPresent()) {
            return SubscriptionDto.from(foundSubscription.get());
        }

        // 새로운 구독 생성
        Interest foundInterest = interestRepository.findByIdWithKeywords(interestId)
            .orElseThrow(() -> new InterestException(InterestErrorCode.INTEREST_NOT_FOUND));
        User proxyUser = userRepository.getReferenceById(requestUserId);

        Subscription newSubscription = Subscription.create(foundInterest, proxyUser);
        subscriptionRepository.save(newSubscription);

        // 관심사의 구독자 수 증가
        foundInterest.increaseSubscriberCount();

        log.info("관심사 구독 요청 성공. SubscriptionId: {}", newSubscription.getId());
        return SubscriptionDto.from(newSubscription);
    }

    /**
     * 관심사 구독 취소
     */
    @Transactional
    public void cancelSubscribeInterest(UUID interestId, UUID requestUserId) {
        subscriptionRepository.findByInterestIdAndUserIdWithInterest(interestId, requestUserId)
            .ifPresent(subscription -> {
                subscriptionRepository.delete(subscription);
                subscription.getInterest().decreaseSubscriberCount();
                log.info("관심사 구독 취소 완료. SubscriptionId: {}", subscription.getId());
            });
    }
}
