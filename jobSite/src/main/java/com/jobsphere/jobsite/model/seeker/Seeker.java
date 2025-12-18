package com.jobsphere.jobsite.model.seeker;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.shared.Address;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name = "seekers")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Seeker {
    @Id private UUID id;
    
    @OneToOne @MapsId @JoinColumn(name = "id")
    private User user;
    
    @Column(name = "first_name", nullable = false) private String firstName;
    @Column(name = "middle_name", nullable = false) private String middleName;
    @Column(name = "last_name") private String lastName;
    @Column(nullable = false) private String phone;
    @Enumerated(EnumType.STRING) private Gender gender;
    @Column(name = "date_of_birth") private LocalDate dateOfBirth;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
    
    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
    
    public enum Gender { MALE, FEMALE }
}