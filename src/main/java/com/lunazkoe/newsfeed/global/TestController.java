package com.lunazkoe.newsfeed.global;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleSource;
import com.lunazkoe.newsfeed.domain.article.repository.ArticleRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {

    private final ArticleRepository articleRepository;

    @Operation(summary = "테스트 컨트롤러 테스트", description = "테스트 컨트롤러를 테스트합니다.")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String hello() {
        log.info("GET/ TestController Test API Call");
        return "Hello TestController!";
    }

    @Operation(summary = "기사 수동 등록", description = "기사를 수동 등록합니다.")
    @GetMapping("/new-article")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void registerDummyArticle() {
        Article article = Article.create(ArticleSource.NAVER, "testsourceurl", "testtitle",
            "testsummary", LocalDateTime.now());
        articleRepository.save(article);
    }
}
