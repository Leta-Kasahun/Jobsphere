CREATE TABLE seeker_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seeker_id UUID NOT NULL REFERENCES seekers(id) ON DELETE CASCADE,
    skill VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seeker_skills_seeker ON seeker_skills(seeker_id);
CREATE INDEX idx_seeker_skills_skill ON seeker_skills(skill);

COMMENT ON TABLE seeker_skills IS 'Stores seeker skills in normalized 1:M relationship';

