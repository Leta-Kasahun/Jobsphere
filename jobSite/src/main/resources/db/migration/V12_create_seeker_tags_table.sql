CREATE TABLE seeker_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seeker_id UUID NOT NULL REFERENCES seekers(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seeker_tags_seeker ON seeker_tags(seeker_id);
CREATE INDEX idx_seeker_tags_tag ON seeker_tags(tag);

