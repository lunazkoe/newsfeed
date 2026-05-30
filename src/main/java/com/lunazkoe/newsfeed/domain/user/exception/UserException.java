package com.lunazkoe.newsfeed.domain.user.exception;

import com.lunazkoe.newsfeed.global.exception.CustomException;
import com.lunazkoe.newsfeed.global.exception.ErrorCode;
import java.util.Map;

public class UserException extends CustomException {

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
