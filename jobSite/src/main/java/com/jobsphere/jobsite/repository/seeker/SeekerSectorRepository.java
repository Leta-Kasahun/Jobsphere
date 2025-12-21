package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerSector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeekerSectorRepository extends JpaRepository<SeekerSector, UUID> {
    List<SeekerSector> findBySeekerId(UUID seekerId);

    Optional<SeekerSector> findBySeekerIdAndSector(UUID seekerId, String sector);

}
