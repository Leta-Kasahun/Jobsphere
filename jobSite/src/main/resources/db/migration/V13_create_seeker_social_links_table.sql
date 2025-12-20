CREATE TABLE seeker_social_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seeker_id UUID NOT NULL REFERENCES seekers(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    url VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seeker_social_links_seeker ON seeker_social_links(seeker_id);
CREATE INDEX idx_seeker_social_links_platform ON seeker_social_links(platform);

