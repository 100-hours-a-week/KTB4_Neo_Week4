package com.ktb.community.repository;

import com.ktb.community.entity.PostEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostEditHistoryRepository extends JpaRepository<PostEditHistory, Long> {
}
