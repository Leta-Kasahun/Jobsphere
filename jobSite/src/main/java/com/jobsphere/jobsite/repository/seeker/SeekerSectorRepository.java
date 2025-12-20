package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerSector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SeekerSectorRepository extends JpaRepository<SeekerSector, UUID> {
    Optional<SeekerSector> findBySeekerId(UUID seekerId);
}

