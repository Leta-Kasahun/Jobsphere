package com.jobsphere.jobsite.model.seeker;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seeker_tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SeekerTag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "seeker_id", nullable = false)
    private UUID seekerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Seeker seeker;

    @Column(name = "tag", length = 100, nullable = false)
    private String tag;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

