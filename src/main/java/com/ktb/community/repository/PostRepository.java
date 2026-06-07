package com.ktb.community.repository;

import com.ktb.community.entity.Post;
import com.ktb.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByDeletedFalse();

    long countByUserAndCreatedAtAfter(User user, LocalDateTime time);
}
