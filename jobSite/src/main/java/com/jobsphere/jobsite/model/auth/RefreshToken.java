package com.jobsphere.jobsite.model.auth;

import com.jobsphere.jobsite.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String tokenHash;
    
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant expiresAt;
    
    @Builder.Default
    private boolean revoked = false;
    
    private String ipAddress;
    
    @Lob
    private String userAgent;
}