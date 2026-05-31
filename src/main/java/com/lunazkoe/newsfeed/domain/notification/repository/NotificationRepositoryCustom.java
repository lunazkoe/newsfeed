package com.lunazkoe.newsfeed.domain.notification.repository;

import com.lunazkoe.newsfeed.domain.notification.dto.NotificationSearchCondition;
import com.lunazkoe.newsfeed.domain.notification.entity.Notification;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;
import java.util.UUID;

public interface NotificationRepositoryCustom {

    CursorPageResponse<Notification>  searchNotifications(NotificationSearchCondition condition, UUID requestUserId);
}
