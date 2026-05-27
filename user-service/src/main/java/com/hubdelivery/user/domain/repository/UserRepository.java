package com.hubdelivery.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hubdelivery.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsBySlackIdAndDeletedAtIsNull(String slackId);

    Optional<User> findBySlackIdAndDeletedAtIsNull(String slackId);

    Optional<User> findByIdAndDeletedAtIsNull(UUID userId);
}
