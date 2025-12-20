package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerSocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SeekerSocialLinkRepository extends JpaRepository<SeekerSocialLink, UUID> {
    List<SeekerSocialLink> findBySeekerId(UUID seekerId);
    void deleteBySeekerId(UUID seekerId);
}

