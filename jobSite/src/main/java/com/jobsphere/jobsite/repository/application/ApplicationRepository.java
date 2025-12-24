package com.jobsphere.jobsite.repository.application;

import com.jobsphere.jobsite.model.application.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    @Query("SELECT a FROM Application a JOIN FETCH a.seeker WHERE a.job.id = :jobId")
    Page<Application> findByJobId(@Param("jobId") UUID jobId, Pageable pageable);

    Page<Application> findBySeekerId(UUID seekerId, Pageable pageable);

    Optional<Application> findByJobIdAndSeekerId(UUID jobId, UUID seekerId);

    Page<Application> findByStatus(String status, Pageable pageable);

    List<Application> findByJobIdAndHiredFlagTrue(UUID jobId);

    @Query("SELECT a FROM Application a JOIN a.job j WHERE j.companyProfile.id = :companyProfileId")
    Page<Application> findByCompanyProfileId(@Param("companyProfileId") UUID companyProfileId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId AND a.status = :status")
    long countByJobIdAndStatus(@Param("jobId") UUID jobId, @Param("status") String status);

    long countByJobId(UUID jobId);

    long countByJobIdAndHiredFlagTrue(UUID jobId);

    @Modifying
    @Query("UPDATE Application a SET a.status = :status, a.reviewedAt = CURRENT_TIMESTAMP WHERE a.id = :applicationId")
    int updateStatus(@Param("applicationId") UUID applicationId, @Param("status") String status);

    @Modifying
    @Query("UPDATE Application a SET a.hiredFlag = :hiredFlag WHERE a.id = :applicationId")
    int updateHiredFlag(@Param("applicationId") UUID applicationId, @Param("hiredFlag") boolean hiredFlag);

    boolean existsByJobIdAndSeekerId(UUID jobId, UUID seekerId);

    @Query("SELECT COUNT(a) FROM Application a JOIN a.job j WHERE j.companyProfile.id = :companyProfileId")
    long countByCompanyProfileId(@Param("companyProfileId") UUID companyProfileId);

    @Query("SELECT COUNT(a) FROM Application a JOIN a.job j WHERE j.companyProfile.id = :companyProfileId AND a.status = :status")
    long countByCompanyProfileIdAndStatus(@Param("companyProfileId") UUID companyProfileId,
            @Param("status") String status);

    @Query("SELECT COUNT(a) FROM Application a JOIN a.job j WHERE j.companyProfile.id = :companyProfileId AND a.appliedAt BETWEEN :start AND :end")
    long countByCompanyProfileIdAndAppliedAtBetween(@Param("companyProfileId") UUID companyProfileId,
            @Param("start") Instant start, @Param("end") Instant end);
}
