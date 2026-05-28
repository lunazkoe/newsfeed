package com.lunazkoe.newsfeed.domain.interest.repository;

import com.lunazkoe.newsfeed.domain.interest.entity.InterestKeyword;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, UUID> {

}
