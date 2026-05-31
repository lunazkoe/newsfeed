package com.lunazkoe.newsfeed.domain.interest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestDto;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestRegisterRequest;
import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestErrorCode;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestException;
import com.lunazkoe.newsfeed.domain.interest.repository.InterestRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @InjectMocks
    private InterestService interestService;

    @Mock
    private InterestRepository interestRepository;

    @Nested
    @DisplayName("관심사 등록(register) 테스트")
    class RegisterTest {

        @Test
        @DisplayName("성공: 유사한 이름의 관심사가 없으면 정상적으로 등록되고 DTO를 반환한다.")
        void register_success() {
            // given
            InterestRegisterRequest request = new InterestRegisterRequest("백엔드 개발", List.of("Java", "Spring", "JPA"));

            // 기존 DB에 유사하지 않은 관심사들만 있다고 가정
            List<String> existingNames = List.of("프론트엔드", "디자인", "데브옵스");
            given(interestRepository.findAllNames()).willReturn(existingNames);

            // when
            InterestDto result = interestService.register(request);

            // then
            assertThat(result.name()).isEqualTo("백엔드 개발");
            assertThat(result.keywords()).containsExactly("Java", "Spring", "JPA");
            assertThat(result.subscriberCount()).isEqualTo(0L);
            assertThat(result.subscribedByMe()).isFalse();

            verify(interestRepository).findAllNames();
            verify(interestRepository).save(any(Interest.class));
        }

        @Test
        @DisplayName("실패: 완전히 동일한 이름의 관심사가 이미 존재하면 예외가 발생한다.")
        void register_fail_exactSameNameExists() {
            // given
            InterestRegisterRequest request = new InterestRegisterRequest("백엔드 개발", List.of("Java"));

            // 기존 DB에 완전히 동일한 이름이 존재한다고 가정
            List<String> existingNames = List.of("프론트엔드", "백엔드 개발", "데브옵스");
            given(interestRepository.findAllNames()).willReturn(existingNames);

            // when & then
            InterestException exception = assertThrows(InterestException.class, () -> {
                interestService.register(request);
            });

            assertThat(exception.getErrorCode()).isEqualTo(InterestErrorCode.SIMILAR_INTEREST_EXISTS);

            verify(interestRepository).findAllNames();
            verify(interestRepository, never()).save(any(Interest.class)); // 저장이 수행되지 않아야 함
        }

        @Test
        @DisplayName("실패: 유사도 80% 이상의 관심사가 이미 존재하면 예외가 발생한다.")
        void register_fail_similarNameExists() {
            // given
            InterestRegisterRequest request = new InterestRegisterRequest("백엔드 개발", List.of("Spring"));

            // 기존 DB에 유사한 이름이 존재한다고 가정
            List<String> existingNames = List.of("벡엔드 개발", "자바");
            given(interestRepository.findAllNames()).willReturn(existingNames);

            // when & then
            InterestException exception = assertThrows(InterestException.class, () -> {
                interestService.register(request);
            });

            assertThat(exception.getErrorCode()).isEqualTo(InterestErrorCode.SIMILAR_INTEREST_EXISTS);

            verify(interestRepository).findAllNames();
            verify(interestRepository, never()).save(any(Interest.class));
        }
    }
}