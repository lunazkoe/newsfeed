package com.lunazkoe.newsfeed.global;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "테스트 컨트롤러 테스트", description = "테스트 컨트롤러를 테스트합니다.")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String hello() {
        log.info("GET/ TestController Test API Call");
        return "Hello TestController!";
    }
}
