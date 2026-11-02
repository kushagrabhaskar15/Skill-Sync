package com.skillsync.user;

import com.skillsync.enums.UserStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByStatusAndIdNot(UserStatus status, Long id, Pageable pageable);

    Page<User> findByStatusAndIdNotAndNameContainingIgnoreCase(UserStatus status, Long id, String name, Pageable pageable);
}


