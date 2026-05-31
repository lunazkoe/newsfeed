package com.lunazkoe.newsfeed.domain.interest.exception;


import com.lunazkoe.newsfeed.global.exception.CustomException;
import com.lunazkoe.newsfeed.global.exception.ErrorCode;
import java.util.Map;

public class InterestException extends CustomException {

    public InterestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InterestException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
