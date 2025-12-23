CREATE TABLE seeker_bio (
    seeker_id UUID PRIMARY KEY REFERENCES seekers(id) ON DELETE CASCADE,
    title VARCHAR(200),
    bio TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_seeker_bio_seeker ON seeker_bio(seeker_id);

