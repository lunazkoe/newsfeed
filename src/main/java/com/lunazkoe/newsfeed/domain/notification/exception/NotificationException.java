package com.lunazkoe.newsfeed.domain.notification.exception;

import com.lunazkoe.newsfeed.global.exception.CustomException;
import com.lunazkoe.newsfeed.global.exception.ErrorCode;
import java.util.Map;

public class NotificationException extends CustomException {

    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
