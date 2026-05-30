package com.lunazkoe.newsfeed.domain.subscription.repository;

import com.lunazkoe.newsfeed.domain.subscription.entity.Subscription;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

}
