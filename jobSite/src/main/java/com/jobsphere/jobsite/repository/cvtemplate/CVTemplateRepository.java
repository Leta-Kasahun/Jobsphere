package com.jobsphere.jobsite.repository.cvtemplate;

import com.jobsphere.jobsite.model.cvtemplate.CVTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CVTemplateRepository extends JpaRepository<CVTemplate, UUID> {

    Page<CVTemplate> findByStatus(String status, Pageable pageable);

    Page<CVTemplate> findByCategory(String category, Pageable pageable);

    Page<CVTemplate> findByCategoryAndStatus(String category, String status, Pageable pageable);

    List<CVTemplate> findByStatus(String status);
}
