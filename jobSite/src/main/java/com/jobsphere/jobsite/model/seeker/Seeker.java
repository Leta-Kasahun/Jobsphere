package com.jobsphere.jobsite.model.seeker;

import com.jobsphere.jobsite.constant.Gender;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "seekers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seeker {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address_id")
    private UUID addressId;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;
}