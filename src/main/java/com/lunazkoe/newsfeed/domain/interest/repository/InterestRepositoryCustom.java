package com.lunazkoe.newsfeed.domain.interest.repository;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestSearchCondition;
import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;

public interface InterestRepositoryCustom {

    CursorPageResponse<Interest> searchInterests(InterestSearchCondition condition);
}
