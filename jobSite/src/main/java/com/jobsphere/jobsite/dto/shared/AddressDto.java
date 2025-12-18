package com.jobsphere.jobsite.dto.shared;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private UUID id;
    private String country;
    private String region;
    private String city;
    private String subCity;
    private String street;
}