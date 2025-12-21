package com.jobsphere.jobsite.model.seeker;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seeker_sector")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SeekerSector {
    @Id
    @Column(name = "seeker_id", nullable = false)
    private UUID seekerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Seeker seeker;

    @Column(name = "sector", length = 100)
    private String sector;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
