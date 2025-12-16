
package com.jobsphere.jobsite.repository.auth;

import com.jobsphere.jobsite.model.auth.Otp;
import com.jobsphere.jobsite.constant.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByEmailAndTypeAndUsedFalseAndExpiresAtAfter(
        String email, OtpType type, Instant now);
    
    @Modifying
    @Query("UPDATE Otp o SET o.used = true WHERE o.email = :email AND o.type = :type")
    void markAllAsUsed(@Param("email") String email, @Param("type") OtpType type);
}


