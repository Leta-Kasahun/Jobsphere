CREATE TABLE seeker_cv (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seeker_id UUID NOT NULL UNIQUE REFERENCES seekers(id) ON DELETE CASCADE,
    title VARCHAR(255),
    about VARCHAR(100),
    details JSONB,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seeker_cv_seeker ON seeker_cv(seeker_id);

COMMENT ON TABLE seeker_cv IS 'Stores seeker CV with flexible JSONB details for experiences, services, skills, and languages';
COMMENT ON COLUMN seeker_cv.details IS 'JSONB field storing flexible CV details: experiences, services, skills, languages';

