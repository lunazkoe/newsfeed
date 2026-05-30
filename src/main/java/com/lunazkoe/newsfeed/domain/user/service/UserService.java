package com.lunazkoe.newsfeed.domain.user.service;

import com.lunazkoe.newsfeed.domain.user.dto.UserDto;
import com.lunazkoe.newsfeed.domain.user.dto.UserLoginRequest;
import com.lunazkoe.newsfeed.domain.user.dto.UserRegisterRequest;
import com.lunazkoe.newsfeed.domain.user.dto.UserUpdateRequest;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public UserDto register(UserRegisterRequest request) {
        // 이메일 중복 확인
        // TODO: 현재는 논리 삭제된 사용자의 중복된 이메일 가입 처리를 하지 않음
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 회원가입 진행 - 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(request.password());
        User newUser = User.create(request.email(), request.nickname(), encodedPassword);
        userRepository.save(newUser);

        log.info("사용자 회원가입 요청 성공. UserId: {}", newUser.getId());
        return UserDto.from(newUser);
    }

    /**
     * 로그인
     */
    public UserDto login(UserLoginRequest request) {
        // 이메일 검증 & 비밀번호 검증
        User foundUser = userRepository.findByEmail(request.email())
            .filter(user -> passwordEncoder.matches(request.password(), user.getEncodedPassword()))
            .orElseThrow(() -> new UserException(UserErrorCode.EMAIL_OR_PASSWORD_INVALID));

        log.info("사용자 로그인 성공. UserId: {}", foundUser.getId());
        return UserDto.from(foundUser);
    }

    /**
     * TODO: 사용자 논리 삭제
     */

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserDto updateNickname(UUID userId, UserUpdateRequest request) {
        // 사용자 조회
        User foundUser = getFoundUserById(userId);

        // 닉네임 변경
        foundUser.updateNickname(request.nickname());

        log.info("사용자 정보 수정 성공. UserId: {}", foundUser.getId());
        return UserDto.from(foundUser);
    }

    /**
     * TODO: 사용자 물리 삭제
     */

    private User getFoundUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
