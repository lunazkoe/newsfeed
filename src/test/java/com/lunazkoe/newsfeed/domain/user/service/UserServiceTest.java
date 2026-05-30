package com.lunazkoe.newsfeed.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.user.dto.UserDto;
import com.lunazkoe.newsfeed.domain.user.dto.UserLoginRequest;
import com.lunazkoe.newsfeed.domain.user.dto.UserRegisterRequest;
import com.lunazkoe.newsfeed.domain.user.dto.UserUpdateRequest;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("회원가입 성공: 정상적인 요청일 경우 유저가 저장되고 DTO를 반환한다.")
        void register_success() {
            // given
            UserRegisterRequest request = new UserRegisterRequest("test@email.com", "test", "password");
            String encodedPassword = "encodedPassword";

            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);

            // when
            UserDto result = userService.register(request);

            // then
            assertThat(result.email()).isEqualTo(request.email());
            assertThat(result.nickname()).isEqualTo(request.nickname());

            verify(userRepository).existsByEmail(request.email());
            verify(passwordEncoder).encode(request.password());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패: 이미 존재하는 이메일의 경우 예외가 발생한다.")
        void register_fail_duplicateEmail() {
            // given
            UserRegisterRequest request = new UserRegisterRequest("test@email.com", "test", "password");
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            // when & then
            UserException exception = assertThrows(UserException.class, () -> {
                userService.register(request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.DUPLICATE_EMAIL);

            verify(userRepository).existsByEmail(request.email());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("성공: 이메일과 비밀번호가 일치하면 DTO를 반환한다.")
        void login_success() {
            // given
            String rawPassword = "password123!";
            String encodedPassword = "encodedPassword!123!";
            UserLoginRequest request = new UserLoginRequest("test@email.com", rawPassword);
            User savedUser = User.create(request.email(), "test", encodedPassword);

            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(savedUser));
            given(passwordEncoder.matches(request.password(), savedUser.getEncodedPassword())).willReturn(true);

            // when
            UserDto result = userService.login(request);

            // then
            assertThat(result.email()).isEqualTo(request.email());
            assertThat(result.nickname()).isEqualTo(savedUser.getNickname());

            verify(userRepository).findByEmail(request.email());
            verify(passwordEncoder).matches(rawPassword, encodedPassword);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 로그인 시도 시 예외가 발생한다")
        void login_fail_userNotFound() {
            // Given
            UserLoginRequest request = new UserLoginRequest("notfound@email.com", "password123!");

            given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

            // When & Then
            UserException exception = assertThrows(UserException.class, () -> {
                userService.login(request);
            });

            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_OR_PASSWORD_INVALID);

            verify(userRepository).findByEmail(request.email());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("실패: 비밀번호가 일치하지 않으면 예외가 발생한다")
        void login_fail_passwordMismatch() {
            // Given
            String rawPassword = "wrongPassword!";
            String encodedPassword = "encodedPassword123!";
            UserLoginRequest request = new UserLoginRequest("test@email.com", rawPassword);
            User savedUser = User.create(request.email(), "테스터", encodedPassword);

            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(savedUser));
            given(passwordEncoder.matches(rawPassword, savedUser.getEncodedPassword())).willReturn(false);

            // When & Then
            UserException exception = assertThrows(UserException.class, () -> {
                userService.login(request);
            });

            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_OR_PASSWORD_INVALID);

            verify(userRepository).findByEmail(request.email());
            verify(passwordEncoder).matches(rawPassword, savedUser.getEncodedPassword());
        }
    }

    @Nested
    @DisplayName("사용자 닉네임 수정(updateNickname) 테스트")
    class UpdateNicknameTest {

        @Test
        @DisplayName("성공: 존재하는 사용자의 닉네임을 변경하면 수정된 DTO를 반환한다")
        void updateNickname_success() {
            // Given
            UUID userId = UUID.randomUUID();
            String originalNickname = "기존닉네임";
            String newNickname = "새로운닉네임";

            // 테스트용 유저 엔티티 생성
            User savedUser = User.create("test@email.com", originalNickname, "encodedPassword!");
            UserUpdateRequest request = new UserUpdateRequest(newNickname);

            // getFoundUserById 내부에서 userRepository.findById를 호출한다고 가정
            given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

            // When
            UserDto result = userService.updateNickname(userId, request);

            // Then
            assertThat(result.nickname()).isEqualTo(newNickname);

            assertThat(savedUser.getNickname()).isEqualTo(newNickname);

            verify(userRepository).findById(userId);

            // save() 메서드는 호출되지 않아야 함 (JPA 변경 감지 활용)
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID로 닉네임 변경 요청 시 예외가 발생한다")
        void updateNickname_fail_userNotFound() {
            // Given
            UUID invalidUserId = UUID.randomUUID();
            UserUpdateRequest request = new UserUpdateRequest("새로운닉네임");

            // 유저 조회 시 빈 Optional 반환되도록 설정
            given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

            // When & Then
            UserException exception = assertThrows(UserException.class, () -> {
                userService.updateNickname(invalidUserId, request);
            });

            assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);

            verify(userRepository).findById(invalidUserId);
        }
    }
}