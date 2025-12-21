package com.jobsphere.jobsite.model.job;

import com.jobsphere.jobsite.model.employer.CompanyProfile;
import com.jobsphere.jobsite.model.shared.Address;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_profile_id", nullable = false)
    private CompanyProfile companyProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "job_type", nullable = false, length = 50)
    private String jobType;

    @Column(name = "workplace_type", nullable = false, length = 50)
    private String workplaceType;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "education_level", nullable = false, length = 100)
    private String educationLevel;

    @Column(name = "gender_requirement", length = 20)
    private String genderRequirement;

    @Column(name = "vacancy_count")
    private Integer vacancyCount;

    @Column(name = "experience_level", length = 100)
    private String experienceLevel;

    @Column(name = "experience_description")
    private String experienceDescription;

    @Column(name = "salary_min", precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 15, scale = 2)
    private BigDecimal salaryMax;

    @Column
    private LocalDate deadline;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "filled_count")
    @Builder.Default
    private Integer filledCount = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
