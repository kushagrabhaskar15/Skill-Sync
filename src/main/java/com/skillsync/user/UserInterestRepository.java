package com.skillsync.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    List<UserInterest> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserInterest ui WHERE ui.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
