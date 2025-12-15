package com.jobsphere.jobsite.model.admin;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    @Id
    private UUID id;
    
    @MapsId
    @OneToOne
    @JoinColumn(name = "id")
    private com.jobsphere.jobsite.model.User user;
    
    private String firstName;
    
    private String lastName;
    
    private String role;
    
    @Builder.Default
    private boolean mustUseOtp = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}