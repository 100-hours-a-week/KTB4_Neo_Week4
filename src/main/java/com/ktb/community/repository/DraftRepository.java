package com.ktb.community.repository;

import com.ktb.community.entity.Draft;
import com.ktb.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DraftRepository extends JpaRepository<Draft, Long> {

    Optional<Draft> findByDraftIdAndUser(Long draftId, User user);
}