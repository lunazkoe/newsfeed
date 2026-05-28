package com.lunazkoe.newsfeed.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.lunazkoe.newsfeed.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class UserTest {

    @Autowired UserRepository userRepository;

    @Test
    void create_user_success() {
        // given
        String email = "user@email.com";
        String nickname = "user";
        String password = "pass";

        // when
        User user = User.create(email, nickname, password);
        userRepository.save(user);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.isDeleted()).isEqualTo(false);
        assertThat(user.getDeletedAt()).isNull();

        log.info("user.getCreatedAt(): {}", user.getCreatedAt().toString());
        log.info("user.getUpdatedAt(): {}", user.getUpdatedAt().toString());
    }
}
