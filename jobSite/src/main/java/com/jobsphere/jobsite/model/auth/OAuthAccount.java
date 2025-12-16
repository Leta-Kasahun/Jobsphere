package com.jobsphere.jobsite.model.auth;

import com.jobsphere.jobsite.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    private String providerEmail;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}