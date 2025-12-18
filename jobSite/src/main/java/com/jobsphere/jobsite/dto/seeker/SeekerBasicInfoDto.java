package com.jobsphere.jobsite.dto.seeker;
import com.jobsphere.jobsite.model.seeker.Seeker.Gender;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeekerBasicInfoDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private Gender gender;
    private LocalDate dateOfBirth;
    private UUID addressId;
}