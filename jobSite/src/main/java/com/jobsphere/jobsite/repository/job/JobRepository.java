package com.jobsphere.jobsite.repository.job;

import com.jobsphere.jobsite.model.job.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

        List<Job> findByCompanyProfileIdAndIsActiveTrue(UUID companyProfileId);

        Page<Job> findByCompanyProfileId(UUID companyProfileId, Pageable pageable);

        Page<Job> findByIsActiveTrue(Pageable pageable);

        List<Job> findByIsActiveTrue();

        @Query("SELECT j FROM Job j JOIN FETCH j.companyProfile cp LEFT JOIN j.address a WHERE j.isActive = true " +
                        "AND j.status IN ('OPEN', 'HIRED') " +
                        "AND (:category IS NULL OR :category = '' OR j.category = :category) " +
                        "AND (:jobType IS NULL OR :jobType = '' OR j.jobType = :jobType) " +
                        "AND (:workplaceType IS NULL OR :workplaceType = '' OR j.workplaceType = :workplaceType) " +
                        "AND (:location IS NULL OR :location = '' OR UPPER(a.city) = UPPER(:location) OR UPPER(a.region) = UPPER(:location)) "
                        +
                        "AND (:isFeatured IS NULL OR j.isFeatured = :isFeatured)")
        Page<Job> findActiveJobsWithFilters(
                        @Param("category") String category,
                        @Param("jobType") String jobType,
                        @Param("workplaceType") String workplaceType,
                        @Param("location") String location,
                        @Param("isFeatured") Boolean isFeatured,
                        Pageable pageable);

        @Query("SELECT j FROM Job j JOIN FETCH j.companyProfile cp WHERE j.id = :jobId AND j.isActive = true AND j.status IN ('OPEN', 'HIRED')")
        Job findActiveJobWithCompanyProfile(@Param("jobId") UUID jobId);

        @Query("SELECT COUNT(j) FROM Job j WHERE j.companyProfile.userId = :userId")
        long countByCompanyProfileUserId(@Param("userId") UUID userId);

        long countByCreatedAtBetween(Instant start, Instant end);

        long countByCompanyProfileId(UUID companyProfileId);

        long countByCompanyProfileIdAndIsActiveTrue(UUID companyProfileId);

        @Query("SELECT j.category, COUNT(j) FROM Job j WHERE j.companyProfile.id = :companyProfileId GROUP BY j.category")
        List<Object[]> countJobsByCategory(@Param("companyProfileId") UUID companyProfileId);
}
