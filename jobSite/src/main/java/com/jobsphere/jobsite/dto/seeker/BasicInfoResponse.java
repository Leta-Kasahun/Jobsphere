package com.jobsphere.jobsite.dto.seeker;

import com.jobsphere.jobsite.constant.Gender;
import com.jobsphere.jobsite.dto.shared.AddressDto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BasicInfoResponse {
    private UUID id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String email;
    private String profileCompletion;
    private String profileImageUrl;
    private AddressDto address;
}