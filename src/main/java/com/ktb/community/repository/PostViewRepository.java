package com.ktb.community.repository;

import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostView;
import com.ktb.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

    Optional<PostView> findByPostAndUser(Post post, User user);

}
