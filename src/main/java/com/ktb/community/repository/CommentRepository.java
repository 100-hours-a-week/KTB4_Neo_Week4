package com.ktb.community.repository;

import com.ktb.community.entity.Comment;
import com.ktb.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostAndParentCommentIsNull(Post post);

    List<Comment> findByParentComment(Comment parentComment);

    boolean existsByParentComment(Comment parentComment);
}
