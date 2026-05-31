package com.lunazkoe.newsfeed.domain.notification.repository;

import com.lunazkoe.newsfeed.domain.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
