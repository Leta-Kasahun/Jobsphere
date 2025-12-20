package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeekerTagRepository extends JpaRepository<SeekerTag, UUID> {
    List<SeekerTag> findBySeekerId(UUID seekerId);
    Optional<SeekerTag> findBySeekerIdAndTag(UUID seekerId, String tag);
    void deleteBySeekerId(UUID seekerId);
    void deleteBySeekerIdAndTag(UUID seekerId, String tag);
}

