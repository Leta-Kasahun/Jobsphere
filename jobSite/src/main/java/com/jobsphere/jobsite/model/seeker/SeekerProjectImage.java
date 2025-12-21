package com.jobsphere.jobsite.model.seeker;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "seeker_project_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SeekerProjectImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private SeekerProject project;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
