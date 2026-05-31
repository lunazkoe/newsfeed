package com.lunazkoe.newsfeed.domain.interest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestDto;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestRegisterRequest;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestUpdateRequest;
import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestErrorCode;
import com.lunazkoe.newsfeed.domain.interest.exception.InterestException;
import com.lunazkoe.newsfeed.domain.interest.repository.InterestRepository;
import com.lunazkoe.newsfeed.domain.subscription.repository.SubscriptionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @InjectMocks
    private InterestService interestService;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Nested
    @DisplayName("관심사 등록(register) 테스트")
    class RegisterTest {

        @Test
        @DisplayName("성공: 유사한 이름의 관심사가 없으면 정상적으로 등록되고 DTO를 반환한다.")
        void register_success() {
            // given
            InterestRegisterRequest request = new InterestRegisterRequest("백엔드 개발", List.of("Java", "Spring", "JPA"));
            List<String> existingNames = List.of("프론트엔드", "디자인", "데브옵스");
            given(interestRepository.findAllNames()).willReturn(existingNames);

            // when
            InterestDto result = interestService.register(request);

            // then
            assertThat(result.name()).isEqualTo("백엔드 개발");
            assertThat(result.keywords()).containsExactly("Java", "Spring", "JPA");
            verify(interestRepository).findAllNames();
            verify(interestRepository).save(any(Interest.class));
        }

        @Test
        @DisplayName("실패: 완전히 동일한 이름의 관심사가 이미 존재하면 예외가 발생한다.")
        void register_fail_exactSameNameExists() {
            // given
            InterestRegisterRequest request = new InterestRegisterRequest("백엔드 개발", List.of("Java"));
            List<String> existingNames = List.of("프론트엔드", "백엔드 개발", "데브옵스");
            given(interestRepository.findAllNames()).willReturn(existingNames);

            // when & then
            InterestException exception = assertThrows(InterestException.class, () -> interestService.register(request));
            assertThat(exception.getErrorCode()).isEqualTo(InterestErrorCode.SIMILAR_INTEREST_EXISTS);
            verify(interestRepository, never()).save(any(Interest.class));
        }

        @Test
        @DisplayName("실패: 유사도 80% 이상의 관심사가 이미 존재하면 예외가 발생한다.")
        void register_fail_similarNameExists() {
            // given
            InterestRegisterRequest request = new InterestRegisterRequest("백엔드 개발", List.of("Spring"));
            List<String> existingNames = List.of("벡엔드 개발", "자바");
            given(interestRepository.findAllNames()).willReturn(existingNames);

            // when & then
            InterestException exception = assertThrows(InterestException.class, () -> interestService.register(request));
            assertThat(exception.getErrorCode()).isEqualTo(InterestErrorCode.SIMILAR_INTEREST_EXISTS);
            verify(interestRepository, never()).save(any(Interest.class));
        }
    }

    @Nested
    @DisplayName("기존 관심사 데이터가 필요한 조작 테스트")
    class ExistingInterestContext {

        private Interest interest;
        private UUID interestId;

        // 💡 이 Nested 클래스 내부에서만 동작하는 setUp
        @BeforeEach
        void setUp() {
            interestId = UUID.randomUUID();
            interest = Interest.create("백엔드 개발", List.of("Java", "Spring"));
            ReflectionTestUtils.setField(interest, "id", interestId);
        }

        @Nested
        @DisplayName("관심사 물리 삭제(hardDelete) 테스트")
        class HardDeleteTest {

            @Test
            @DisplayName("성공: 관심사 ID가 존재하면 연관된 구독을 먼저 삭제하고 관심사를 물리 삭제한다.")
            void hardDelete_success() {
                given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

                interestService.hardDelete(interestId);

                verify(subscriptionRepository).deleteByInterestId(interestId);
                verify(interestRepository).delete(interest);
            }

            @Test
            @DisplayName("실패: 존재하지 않는 관심사 ID로 삭제 요청 시 예외가 발생한다.")
            void hardDelete_fail_interestNotFound() {
                given(interestRepository.findById(interestId)).willReturn(Optional.empty());

                InterestException exception = assertThrows(InterestException.class, () -> interestService.hardDelete(interestId));
                assertThat(exception.getErrorCode()).isEqualTo(InterestErrorCode.INTEREST_NOT_FOUND);

                verify(subscriptionRepository, never()).deleteByInterestId(any());
                verify(interestRepository, never()).delete(any());
            }
        }

        @Nested
        @DisplayName("관심사 키워드 수정(updateKeywords) 테스트")
        class UpdateKeywordsTest {

            @Test
            @DisplayName("성공: 관심사 ID가 존재하면 키워드를 성공적으로 교체하고 DTO를 반환한다.")
            void updateKeywords_success() {
                List<String> newKeywords = List.of("Node.js", "Express");
                InterestUpdateRequest request = new InterestUpdateRequest(newKeywords);
                given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

                InterestDto result = interestService.updateKeywords(interestId, request);

                assertThat(result.id()).isEqualTo(interestId);
                assertThat(result.keywords()).containsExactly("Node.js", "Express");
                verify(interestRepository, never()).save(any(Interest.class)); // 더티 체킹 검증
            }

            @Test
            @DisplayName("실패: 존재하지 않는 관심사 ID로 수정 요청 시 예외가 발생한다.")
            void updateKeywords_fail_interestNotFound() {
                InterestUpdateRequest request = new InterestUpdateRequest(List.of("Go", "Docker"));
                given(interestRepository.findById(interestId)).willReturn(Optional.empty());

                InterestException exception = assertThrows(InterestException.class, () -> interestService.updateKeywords(interestId, request));
                assertThat(exception.getErrorCode()).isEqualTo(InterestErrorCode.INTEREST_NOT_FOUND);
            }
        }
    }
}