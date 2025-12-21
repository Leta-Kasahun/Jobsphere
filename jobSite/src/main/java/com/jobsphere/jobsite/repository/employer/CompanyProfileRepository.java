package com.jobsphere.jobsite.repository.employer;

import com.jobsphere.jobsite.model.employer.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, UUID> {
    Optional<CompanyProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}



