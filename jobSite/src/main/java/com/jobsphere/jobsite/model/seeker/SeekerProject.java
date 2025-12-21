package com.jobsphere.jobsite.model.seeker;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seeker_projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SeekerProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "seeker_id", nullable = false)
    private UUID seekerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Seeker seeker;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "project_url", length = 500)
    private String projectUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<SeekerProjectImage> images;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "video_type", length = 20)
    @Builder.Default
    private String videoType = "UPLOAD"; // UPLOAD, YOUTUBE

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
