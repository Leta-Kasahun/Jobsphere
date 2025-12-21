package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerCV;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SeekerCVRepository extends JpaRepository<SeekerCV, UUID> {
    Optional<SeekerCV> findBySeekerId(UUID seekerId);
}

