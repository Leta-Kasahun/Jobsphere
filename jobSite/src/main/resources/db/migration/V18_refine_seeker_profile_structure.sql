-- Refine seeker profile structure to support professional requirements

-- 1. Update skills table to include proficiency
ALTER TABLE seeker_skills ADD COLUMN IF NOT EXISTS proficiency VARCHAR(50) DEFAULT 'BEGINNER';

-- 2. Modify seeker_sector to support multiple sectors per seeker
-- First, drop the primary key constraint if it's strictly on seeker_id
ALTER TABLE seeker_sector DROP CONSTRAINT IF EXISTS seeker_sector_pkey;
-- Add a unique ID for each sector entry
ALTER TABLE seeker_sector ADD COLUMN IF NOT EXISTS id UUID PRIMARY KEY DEFAULT gen_random_uuid();
-- Ensure seeker_id is still indexed for performance
CREATE INDEX IF NOT EXISTS idx_seeker_sector_seeker_lookup ON seeker_sector(seeker_id);

-- 3. Multi-image support for projects
CREATE TABLE IF NOT EXISTS seeker_project_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES seeker_projects(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_project_images_project ON seeker_project_images(project_id);

-- 4. Mark video type for projects
ALTER TABLE seeker_projects ADD COLUMN IF NOT EXISTS video_type VARCHAR(20) DEFAULT 'UPLOAD'; -- 'UPLOAD' or 'YOUTUBE'

COMMENT ON COLUMN seeker_skills.proficiency IS 'Skill level: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT';
COMMENT ON TABLE seeker_project_images IS 'Stores multiple images for a single project';
COMMENT ON COLUMN seeker_projects.video_type IS 'Type of video: UPLOADed file or YOUTUBE link';
