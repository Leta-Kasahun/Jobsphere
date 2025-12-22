package com.jobsphere.jobsite.dto.seeker;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FullProfileResponse {
    private BasicInfoResponse basicInfo;
    private BioDto bio;
    private List<SkillDto> skills;
    private List<ProjectDto> projects;
    private List<SectorDto> sectors;
    private List<TagDto> tags;
    private List<SocialLinkDto> socialLinks;
    private CVDto cv;
}
