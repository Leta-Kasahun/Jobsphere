package com.jobsphere.jobsite.model.seeker;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seeker_bio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SeekerBio {
    @Id
    @Column(name = "seeker_id")
    private UUID seekerId;

    @OneToOne
    @JoinColumn(name = "seeker_id", referencedColumnName = "id")
    @MapsId
    private Seeker seeker;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}

