package com.hubdelivery.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hubdelivery.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsBySlackIdAndDeletedAtIsNull(String slackId);

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    Optional<User> findBySlackIdAndDeletedAtIsNull(String slackId);

}
