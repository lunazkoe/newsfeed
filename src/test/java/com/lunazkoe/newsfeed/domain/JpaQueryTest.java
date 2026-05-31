package com.lunazkoe.newsfeed.domain;

import com.lunazkoe.newsfeed.domain.article.entity.Article;
import com.lunazkoe.newsfeed.domain.article.entity.ArticleSource;
import com.lunazkoe.newsfeed.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import com.lunazkoe.newsfeed.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsfeed.domain.commentlike.repository.CommentLikeRepository;
import com.lunazkoe.newsfeed.domain.commentlike.service.CommentLikeService;
import com.lunazkoe.newsfeed.domain.user.entity.User;
import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class JpaQueryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentLikeService commentLikeService;

    @Autowired
    EntityManager em;

    @Test
    void test() {
        Article article = Article.create(
            ArticleSource.NAVER,
            "testsourceurl",
            "testtitle",
            "testsummary",
            LocalDateTime.now()
        );
        articleRepository.save(article);

        User user1 = User.create("test1@email.com", "tester", "testpassword");
        userRepository.save(user1);

        User user2 = User.create("test2@email.com", "tester", "testpassword");
        userRepository.save(user2);
        UUID requestUserId = user2.getId();

        Comment comment = Comment.create(article, user1, "testcontent");
        commentRepository.save(comment);
        UUID commentId = comment.getId();

        em.flush();
        em.clear();

        commentLikeService.likeComment(commentId, requestUserId);
    }
}
