
CREATE TABLE job_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seeker_id UUID NOT NULL REFERENCES seekers(id) ON DELETE CASCADE,

    keywords TEXT NOT NULL,
    category VARCHAR(100),
    job_type VARCHAR(50),
    preferred_location VARCHAR(100),

    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Indexes for performance
CREATE INDEX idx_job_alerts_seeker ON job_alerts(seeker_id);
CREATE INDEX idx_job_alerts_active ON job_alerts(is_active);

-- Add index on created_at for sorting
CREATE INDEX idx_job_alerts_created_at ON job_alerts(created_at DESC);

-- Comments for documentation
COMMENT ON TABLE job_alerts IS 'Job alerts created by seekers to receive notifications for matching job postings';
COMMENT ON COLUMN job_alerts.keywords IS 'Comma-separated keywords to match against job titles and descriptions';
COMMENT ON COLUMN job_alerts.category IS 'Job category filter (e.g., IT, Healthcare, Finance)';
COMMENT ON COLUMN job_alerts.job_type IS 'Type of job (e.g., FULL_TIME, PART_TIME, CONTRACT)';
COMMENT ON COLUMN job_alerts.preferred_location IS 'Preferred job location for filtering';
COMMENT ON COLUMN job_alerts.is_active IS 'Whether the alert is active and should send notifications';