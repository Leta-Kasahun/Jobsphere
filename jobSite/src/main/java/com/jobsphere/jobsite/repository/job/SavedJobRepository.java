package com.jobsphere.jobsite.repository.job;

import com.jobsphere.jobsite.model.job.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {
    boolean existsByJobIdAndUserId(UUID jobId, UUID userId);

    Optional<SavedJob> findByJobIdAndUserId(UUID jobId, UUID userId);

    Page<SavedJob> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
