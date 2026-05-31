package com.lunazkoe.newsfeed.domain.subscription.repository;

import com.lunazkoe.newsfeed.domain.subscription.entity.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("SELECT s FROM Subscription s JOIN FETCH s.interest WHERE s.interest.id = :interestId AND s.user.id = :userId")
    Optional<Subscription> findByInterestIdAndUserIdWithInterest(@Param("interestId") UUID interestId, @Param("userId") UUID userId);

    @Query("SELECT DISTINCT s FROM Subscription s JOIN FETCH s.interest i JOIN FETCH i.keywords WHERE s.interest.id = :interestId AND s.user.id = :userId")
    Optional<Subscription> findByInterestIdAndUserIdWithInterestAndKeywords(@Param("interestId") UUID interestId, @Param("userId") UUID userId);

    // 해당 관심사의 모든 구독 삭제
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Subscription s WHERE s.interest.id = :interestId")
    void deleteByInterestId(@Param("interestId") UUID interestId);
}
