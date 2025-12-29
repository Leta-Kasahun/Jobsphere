package com.jobsphere.jobsite.repository.admin;

import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.model.admin.Admin;
import com.jobsphere.jobsite.model.admin.AdminOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminOtpRepository extends JpaRepository<AdminOtp, UUID> {
    List<AdminOtp> findByAdminAndTypeAndUsedFalseAndExpiresAtAfter(
            Admin admin, OtpType type, Instant now);
}