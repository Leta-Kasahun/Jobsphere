package com.jobsphere.jobsite.repository.job;

import com.jobsphere.jobsite.model.job.JobLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface JobLikeRepository extends JpaRepository<JobLike, UUID> {
    boolean existsByJobIdAndUserId(UUID jobId, UUID userId);

    Optional<JobLike> findByJobIdAndUserId(UUID jobId, UUID userId);

    long countByJobId(UUID jobId);

    List<JobLike> findByUserId(UUID userId);
}
