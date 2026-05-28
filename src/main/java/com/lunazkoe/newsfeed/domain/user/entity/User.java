package com.lunazkoe.newsfeed.domain.user.entity;

import static com.lunazkoe.newsfeed.global.entity.BaseSoftDeleteEntity.IS_DELETED_FALSE_ONLY;

import com.lunazkoe.newsfeed.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(IS_DELETED_FALSE_ONLY)
public class User extends BaseSoftDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private User(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    public static User create(String email, String nickname, String password) {
        return new User(email, nickname, password);
    }

    // == 비즈니스 메서드 ==
    /**
     * 사용자 닉네임 수정
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
