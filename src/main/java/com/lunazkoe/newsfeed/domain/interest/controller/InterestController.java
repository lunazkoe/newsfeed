package com.lunazkoe.newsfeed.domain.interest.controller;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestDto;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestRegisterRequest;
import com.lunazkoe.newsfeed.domain.interest.service.InterestService;
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
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestService interestService;

    @Operation(summary = "관심사 등록", description = "새로운 관심사를 등록합니다.")
    @PostMapping()
    public ResponseEntity<InterestDto> createInterest(@Valid @RequestBody InterestRegisterRequest request) {
        InterestDto response = interestService.register(request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
