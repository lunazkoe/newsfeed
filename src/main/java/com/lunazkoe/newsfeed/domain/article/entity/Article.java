package com.lunazkoe.newsfeed.domain.article.entity;

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
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(IS_DELETED_FALSE_ONLY)
public class Article extends BaseSoftDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "article_id")
    private UUID id;

}
