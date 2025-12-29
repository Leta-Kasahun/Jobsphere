package com.jobsphere.jobsite.dto.seeker;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SeekerSummaryResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String title;
    private List<String> skills;
    private String city;
    private String country;
    private String completion;
}
