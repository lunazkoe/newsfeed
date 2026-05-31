package com.lunazkoe.newsfeed.domain.comment.exception;

import com.lunazkoe.newsfeed.global.exception.CustomException;
import com.lunazkoe.newsfeed.global.exception.ErrorCode;
import java.util.Map;

public class CommentException extends CustomException {

    public CommentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
