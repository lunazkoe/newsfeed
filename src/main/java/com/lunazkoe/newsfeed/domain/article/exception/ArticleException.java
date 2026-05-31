package com.lunazkoe.newsfeed.domain.article.exception;

import com.lunazkoe.newsfeed.global.exception.CustomException;
import com.lunazkoe.newsfeed.global.exception.ErrorCode;
import java.util.Map;

public class ArticleException extends CustomException {

    public ArticleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ArticleException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
