package com.jobsphere.jobsite.repository.employer;

import com.jobsphere.jobsite.model.employer.CompanyVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, UUID> {

    Optional<CompanyVerification> findByUserId(UUID userId);

    Optional<CompanyVerification> findByUserIdAndStatus(UUID userId, String status);

    Optional<CompanyVerification> findByVerificationCodeAndCodeUsedFalse(String verificationCode);

    List<CompanyVerification> findByStatusOrderBySubmittedAtAsc(String status);

    @Query("SELECT cv FROM CompanyVerification cv WHERE cv.status = :status ORDER BY cv.submittedAt ASC")
    List<CompanyVerification> findPendingVerifications(@Param("status") String status);

    @Query("SELECT cv FROM CompanyVerification cv JOIN User u ON cv.userId = u.id WHERE cv.status = :status ORDER BY cv.submittedAt ASC")
    List<CompanyVerification> findPendingVerificationsWithUser(@Param("status") String status);

    boolean existsByUserIdAndStatus(UUID userId, String status);
}
