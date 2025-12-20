package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.Seeker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SeekerRepository extends JpaRepository<Seeker, UUID> {
}