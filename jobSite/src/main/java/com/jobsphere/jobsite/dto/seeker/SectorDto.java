package com.jobsphere.jobsite.dto.seeker;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorDto {
    private UUID id;

    @Size(max = 100)
    private String sector;
}
