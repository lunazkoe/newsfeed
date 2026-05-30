package com.lunazkoe.newsfeed.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.lunazkoe.newsfeed.domain.user.dto.UserDto;
import com.lunazkoe.newsfeed.domain.user.dto.UserRegisterRequest;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
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