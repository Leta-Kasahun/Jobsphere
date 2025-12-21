
package com.jobsphere.jobsite.repository;

import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.constant.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    boolean existsByGoogleId(String googleId);
}