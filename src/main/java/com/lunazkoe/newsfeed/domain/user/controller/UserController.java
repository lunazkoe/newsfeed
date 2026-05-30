package com.lunazkoe.newsfeed.domain.user.controller;

import com.lunazkoe.newsfeed.domain.user.dto.UserDto;
import com.lunazkoe.newsfeed.domain.user.dto.UserLoginRequest;
import com.lunazkoe.newsfeed.domain.user.dto.UserRegisterRequest;
import com.lunazkoe.newsfeed.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping()
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("[UserController] 회원가입 요청 수신");
        UserDto response = userService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("사용자 로그인 요청 수신");
        UserDto response = userService.login(request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
