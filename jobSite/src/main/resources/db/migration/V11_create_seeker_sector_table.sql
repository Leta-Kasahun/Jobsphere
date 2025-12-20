CREATE TABLE seeker_sector (
    seeker_id UUID PRIMARY KEY REFERENCES seekers(id) ON DELETE CASCADE,
    sector VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seeker_sector_seeker ON seeker_sector(seeker_id);

