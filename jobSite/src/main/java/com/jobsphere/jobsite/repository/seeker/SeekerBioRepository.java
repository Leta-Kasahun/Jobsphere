package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerBio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SeekerBioRepository extends JpaRepository<SeekerBio, UUID> {
    Optional<SeekerBio> findBySeekerId(UUID seekerId);
    void deleteBySeekerId(UUID seekerId);
}

