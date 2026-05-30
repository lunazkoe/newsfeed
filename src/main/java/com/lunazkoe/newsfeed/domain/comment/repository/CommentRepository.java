package com.lunazkoe.newsfeed.domain.comment.repository;

import com.lunazkoe.newsfeed.domain.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

}
