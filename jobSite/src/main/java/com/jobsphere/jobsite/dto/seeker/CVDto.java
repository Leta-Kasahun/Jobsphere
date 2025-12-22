package com.jobsphere.jobsite.dto.seeker;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVDto {
    private UUID id;

    @Size(max = 255)
    private String title;

    @Size(max = 100)
    private String about;

    private String cvUrl;
    private String fileName;
    private String fileSize;

    private Map<String, Object> details;
}
