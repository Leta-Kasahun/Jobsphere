package com.jobsphere.jobsite.model.auth;

import com.jobsphere.jobsite.constant.OtpType;
import com.jobsphere.jobsite.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    private String email;
    
    private String codeHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType type;
    
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant expiresAt;
    
    @Builder.Default
    private boolean used = false;
    
    @Builder.Default
    private Integer attempts = 0;
}