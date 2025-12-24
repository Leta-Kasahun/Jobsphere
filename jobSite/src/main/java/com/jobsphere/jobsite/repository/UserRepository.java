package com.jobsphere.jobsite.repository;

import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.constant.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    boolean existsByGoogleId(String googleId);

    List<User> findByUserType(UserType userType);

    long countByCreatedAtBetween(Instant start, Instant end);

    long countByUserType(UserType userType);
}