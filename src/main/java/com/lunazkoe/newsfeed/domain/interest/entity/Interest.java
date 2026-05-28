package com.lunazkoe.newsfeed.domain.interest.entity;

import com.lunazkoe.newsfeed.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "interest_id")
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterestKeyword> keywords = new ArrayList<>();

    private long subscriptionCount = 0L;

    private Interest(String name, List<String> keywords) {
        this.name = name;
        for (String keyword : keywords) {
            this.addKeyword(keyword);
        }
    }

    // == 연관관계 편의 메서드 ==
    public void addKeyword(String keyword) {
        InterestKeyword interestKeyword = new InterestKeyword(this, keyword);
        this.keywords.add(interestKeyword);
    }

    // == 생성 메서드 ==
    public static Interest create(String name, List<String> keywords) {
        return new Interest(name, keywords);
    }

    // == 비즈니스 메서드 ==
    /**
     * 키워드 수정
     */
    public void updateKeywords(List<String> keywords) {
        this.keywords.clear();
        for (String keyword : keywords) {
            this.addKeyword(keyword);
        }
    }
}
