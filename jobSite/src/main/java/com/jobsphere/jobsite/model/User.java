package com.jobsphere.jobsite.model;

import com.jobsphere.jobsite.constant.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String passwordHash;
    
    private String googleId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 20)
    private UserType userType;
    
    @Builder.Default
    private boolean isActive = true;
    
    @Builder.Default
    private boolean emailVerified = false;
    
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    private Instant accountLockedUntil;
    
    private Instant lastLogin;
    
    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    private Instant deletedAt;
}