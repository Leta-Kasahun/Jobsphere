package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.SeekerProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SeekerProjectImageRepository extends JpaRepository<SeekerProjectImage, UUID> {
}
