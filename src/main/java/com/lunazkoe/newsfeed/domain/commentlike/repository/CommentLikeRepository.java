package com.lunazkoe.newsfeed.domain.commentlike.repository;

import com.lunazkoe.newsfeed.domain.commentlike.entity.CommentLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    @Query("select cl from CommentLike cl join fetch cl.comment join fetch cl.user where cl.comment.id = :commentId and cl.user.id = :userId")
    Optional<CommentLike> findByCommentIdAndUserId(@Param("commentId") UUID commentId, @Param("userId") UUID userId);
}
