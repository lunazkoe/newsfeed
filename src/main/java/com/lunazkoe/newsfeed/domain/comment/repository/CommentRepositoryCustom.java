package com.lunazkoe.newsfeed.domain.comment.repository;

import com.lunazkoe.newsfeed.domain.comment.dto.CommentSearchCondition;
import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;

public interface CommentRepositoryCustom {
    CursorPageResponse<Comment> searchComments(CommentSearchCondition condition);
}
