package com.jobsphere.jobsite.model.auth;

import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_otps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "code_hash", nullable = false)
    private String codeHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType type;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Builder.Default
    private Boolean used = false;
    
    @Version
    private Integer version;
    
    @Builder.Default
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;
    
    @Column(name = "locked_until")
    private Instant lockedUntil;
}