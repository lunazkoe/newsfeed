package com.lunazkoe.newsfeed.domain.comment.repository;

import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

    @EntityGraph(attributePaths = {"user"})
    Optional<Comment> findWithUserById(UUID commentId);
}
