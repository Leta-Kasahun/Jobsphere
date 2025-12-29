package com.jobsphere.jobsite.repository.seeker;

import com.jobsphere.jobsite.model.seeker.Seeker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeekerRepository extends JpaRepository<Seeker, UUID> {
    @Query("SELECT DISTINCT s FROM Seeker s " +
            "LEFT JOIN SeekerBio b ON s.id = b.seekerId " +
            "LEFT JOIN Address a ON s.addressId = a.id " +
            "WHERE (:query IS NULL OR :query = '' " +
            "OR UPPER(s.firstName) LIKE UPPER(CONCAT('%', :query, '%')) " +
            "OR UPPER(s.lastName) LIKE UPPER(CONCAT('%', :query, '%')) " +
            "OR UPPER(b.title) LIKE UPPER(CONCAT('%', :query, '%')) " +
            "OR EXISTS (SELECT sk FROM SeekerSkill sk WHERE sk.seekerId = s.id AND UPPER(sk.skill) LIKE UPPER(CONCAT('%', :query, '%'))))")
    Page<Seeker> findBySearchQuery(@Param("query") String query, Pageable pageable);
}