package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeekerSkillRepository extends JpaRepository<SeekerSkill, UUID> {
    List<SeekerSkill> findBySeekerId(UUID seekerId);
    Optional<SeekerSkill> findByIdAndSeekerId(UUID id, UUID seekerId);
    Optional<SeekerSkill> findBySeekerIdAndSkill(UUID seekerId, String skill);
}

