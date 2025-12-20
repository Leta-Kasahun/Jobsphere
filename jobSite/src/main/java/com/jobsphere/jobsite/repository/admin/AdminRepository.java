package com.jobsphere.jobsite.repository.admin;

import com.jobsphere.jobsite.model.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByEmailIgnoreCase(String email);
}
