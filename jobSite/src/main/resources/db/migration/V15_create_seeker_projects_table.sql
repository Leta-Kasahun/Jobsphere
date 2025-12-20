CREATE TABLE seeker_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seeker_id UUID NOT NULL REFERENCES seekers(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    project_url VARCHAR(500),
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seeker_projects_seeker ON seeker_projects(seeker_id);
CREATE INDEX idx_seeker_projects_title ON seeker_projects(title);

COMMENT ON TABLE seeker_projects IS 'Stores seeker portfolio projects with optional media files';
COMMENT ON COLUMN seeker_projects.image_url IS 'URL of project image stored in Cloudinary';
COMMENT ON COLUMN seeker_projects.video_url IS 'URL of project video stored in Cloudinary';

