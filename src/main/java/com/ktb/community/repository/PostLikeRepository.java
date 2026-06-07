package com.ktb.community.repository;

import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostLike;
import com.ktb.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, User user);

    Optional<PostLike> findByPostAndUser(Post post, User user);
}
