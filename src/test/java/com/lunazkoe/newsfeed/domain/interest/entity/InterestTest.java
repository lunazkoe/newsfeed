package com.lunazkoe.newsfeed.domain.interest.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

import com.lunazkoe.newsfeed.domain.interest.repository.InterestKeywordRepository;
import com.lunazkoe.newsfeed.domain.interest.repository.InterestRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@Transactional
class InterestTest {

    @Autowired
    InterestRepository interestRepository;

    @Autowired
    InterestKeywordRepository interestKeywordRepository;

    @Test
    void 관심사_생성_성공_테스트() {
        // given
        String name = "interest";
        List<String> keywords = List.of("안녕", "너는");

        // when
        Interest interest = Interest.create(name, keywords);
        interestRepository.save(interest);

        // then
//        assertThat(interest.getKeywords().get(0).getKeyword()).isEqualTo("안녕");
//        assertThat(interest.getKeywords().get(1).getKeyword()).isEqualTo("너는");
//
//        log.info("interestId: {}", interest.getId());
//        assertThat(interest.getKeywords().get(0).getInterest().getId()).isEqualTo(interest.getId());
    }
}