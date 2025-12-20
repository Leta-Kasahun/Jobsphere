package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerProject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeekerProjectRepository extends JpaRepository<SeekerProject, UUID> {
    List<SeekerProject> findBySeekerId(UUID seekerId);
    List<SeekerProject> findBySeekerIdAndTitleContainingIgnoreCase(UUID seekerId, String title);
    Optional<SeekerProject> findByIdAndSeekerId(UUID id, UUID seekerId);
}

