package com.lunazkoe.newsfeed.domain.interest.repository;

import static com.lunazkoe.newsfeed.domain.interest.entity.QInterest.interest;
import static com.lunazkoe.newsfeed.domain.interest.entity.QInterestKeyword.interestKeyword;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestSearchCondition;
import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.global.dto.CursorPageResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Interest> searchInterests(InterestSearchCondition condition) {


        return null;
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return interest.name.contains(keyword)
            .or(interestKeyword.keyword.contains(keyword));
    }

}
