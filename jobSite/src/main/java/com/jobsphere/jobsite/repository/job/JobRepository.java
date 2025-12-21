package com.jobsphere.jobsite.repository.job;

import com.jobsphere.jobsite.model.job.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findByCompanyProfileIdAndIsActiveTrue(UUID companyProfileId);

    Page<Job> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.isActive = true " +
           "AND (:category IS NULL OR j.category = :category) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:workplaceType IS NULL OR j.workplaceType = :workplaceType) " +
           "AND (:city IS NULL OR j.address.city = :city)")
    Page<Job> findActiveJobsWithFilters(
        @Param("category") String category,
        @Param("jobType") String jobType,
        @Param("workplaceType") String workplaceType,
        @Param("city") String city,
        Pageable pageable
    );

    @Query("SELECT j FROM Job j JOIN FETCH j.companyProfile cp WHERE j.id = :jobId AND j.isActive = true")
    Job findActiveJobWithCompanyProfile(@Param("jobId") UUID jobId);
}
