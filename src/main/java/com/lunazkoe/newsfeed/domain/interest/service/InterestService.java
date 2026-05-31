package com.lunazkoe.newsfeed.domain.interest.service;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestDto;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestRegisterRequest;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestUpdateRequest;
import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestErrorCode;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestException;
import com.lunazkoe.newsfeed.domain.interest.repository.InterestRepository;
import com.lunazkoe.newsfeed.domain.subscription.repository.SubscriptionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 관심사 목록 조회
     */

    /**
     * 관심사 등록
     */
    @Transactional
    public InterestDto register(InterestRegisterRequest request) {
        // 모든 관심사 이름 조회
        List<String> existingInterestNames = interestRepository.findAllNames();

        // 유사한 관심사 중복 검증
        // TODO: OOM 문제 예상 - 모든 관심사 이름을 올려 놓을 경우 문제가 발생할 수 있을 것으로 예상
        if (InterestSimilarityChecker.hasSimilarName(request.name(), existingInterestNames)) {
            log.info("유사한 관심사가 존재합니다. InterestName: {}", request.name());
            throw new InterestException(InterestErrorCode.SIMILAR_INTEREST_EXISTS);
        }

        // 새로운 관심사 생성
        Interest newInterest = Interest.create(request.name(), request.keywords());
        interestRepository.save(newInterest);

        log.info("관심사 등록 요청 완료. InterestId: {}", newInterest.getId());
        return InterestDto.from(newInterest, false);
    }

    /**
     * 관심사 물리 삭제
     */
    @Transactional
    public void hardDelete(UUID interestId) {
        Interest foundInterest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestException(InterestErrorCode.INTEREST_NOT_FOUND));

        // 관심사 관련 Subscription 삭제
        subscriptionRepository.deleteByInterestId(interestId);

        // 관심사 삭제
        interestRepository.delete(foundInterest);
        log.info("관심사 물리 삭제 요청 완료. InterestId: {}", foundInterest.getId());
    }

    /**
     * 관심사 정보 수정
     */
    @Transactional
    public InterestDto updateKeywords(UUID interestId, InterestUpdateRequest request) {
        // 관심사 찾기
        Interest foundInterest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestException(InterestErrorCode.INTEREST_NOT_FOUND));

        // 키워드 수정
        foundInterest.updateKeywords(request.keywords());

        log.info("관심사 정보(키워드) 수정 완료. InterestId: {}", foundInterest.getId());
        return InterestDto.from(foundInterest, null);
    }
}
