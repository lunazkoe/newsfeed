package com.lunazkoe.newsfeed.domain.commentlike.repository;

import com.lunazkoe.newsfeed.domain.commentlike.entity.CommentLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

}
