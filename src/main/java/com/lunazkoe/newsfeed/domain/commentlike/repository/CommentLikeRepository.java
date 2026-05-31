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

    @Query("SELECT cl FROM CommentLike cl JOIN FETCH cl.comment WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    Optional<CommentLike> findByCommentIdAndUserIdWithComment(@Param("commentId") UUID commentId, @Param("userId") UUID userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId)", nativeQuery = true)
    boolean existsByCommentIdAndUserIdDirectly( @Param("commentId") UUID commentId, @Param("userId") UUID userId);
}
