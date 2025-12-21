CREATE TABLE job_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    seeker_id UUID NOT NULL REFERENCES seekers(id) ON DELETE CASCADE,
    cover_letter TEXT NOT NULL CHECK (char_length(cover_letter) <= 10000),
    expected_salary NUMERIC,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    applied_at TIMESTAMPTZ DEFAULT now(),
    reviewed_at TIMESTAMPTZ,
    hired_flag BOOLEAN DEFAULT false,
    notes TEXT,
    UNIQUE (job_id, seeker_id)
);

CREATE INDEX idx_job_applications_job_id ON job_applications(job_id);
CREATE INDEX idx_job_applications_seeker_id ON job_applications(seeker_id);
CREATE INDEX idx_job_applications_status ON job_applications(status);
CREATE INDEX idx_job_applications_hired_flag ON job_applications(hired_flag);
CREATE INDEX idx_job_applications_applied_at ON job_applications(applied_at DESC);

COMMENT ON TABLE job_applications IS 'Job applications submitted by seekers to job postings';
COMMENT ON COLUMN job_applications.cover_letter IS 'Cover letter with maximum 10,000 characters';
COMMENT ON COLUMN job_applications.expected_salary IS 'Seeker expected salary for the position';
COMMENT ON COLUMN job_applications.status IS 'Application status: PENDING (awaiting review), APPROVED (shortlisted), REJECTED (not selected)';
COMMENT ON COLUMN job_applications.hired_flag IS 'Whether the seeker was hired for this position';
COMMENT ON COLUMN job_applications.notes IS 'Optional employer feedback and notes about the application';
