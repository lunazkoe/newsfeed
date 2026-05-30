package com.lunazkoe.newsfeed.domain.user.service;

import com.lunazkoe.newsfeed.domain.user.dto.UserDto;
import com.lunazkoe.newsfeed.domain.user.dto.UserRegisterRequest;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsfeed.domain.user.exception.UserException;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
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

        log.info("회원가입 요청 성공. UserId: {}", newUser.getId());
        return UserDto.from(newUser);
    }
}
