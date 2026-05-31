package com.lunazkoe.newsfeed.domain.interest.controller;

import com.lunazkoe.newsfeed.domain.interest.dto.InterestDto;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestRegisterRequest;
import com.lunazkoe.newsfeed.domain.interest.dto.InterestUpdateRequest;
import com.lunazkoe.newsfeed.domain.interest.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<InterestDto> register(@Valid @RequestBody InterestRegisterRequest request) {
        InterestDto response = interestService.register(request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @Operation(summary = "관심사 물리 삭제", description = "관심사를 물리적으로 삭제합니다.")
    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID interestId) {
        interestService.hardDelete(interestId);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Operation(summary = "관심사 정보 수정", description = "관심사의 키워드를 수정합니다.")
    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> updateKeywords(@PathVariable UUID interestId, @Valid @RequestBody InterestUpdateRequest request) {
        InterestDto response = interestService.updateKeywords(interestId, request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
