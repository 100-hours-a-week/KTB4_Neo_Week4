package com.ktb.community.repository;

import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostReport;
import com.ktb.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    boolean existsByPostAndUser(Post post, User user);

    long countByPost(Post post);

}
