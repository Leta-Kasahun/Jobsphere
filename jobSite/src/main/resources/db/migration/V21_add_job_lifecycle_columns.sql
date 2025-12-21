ALTER TABLE jobs
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
    CHECK (status IN ('OPEN', 'CLOSED', 'HIRED')),

ADD COLUMN filled_count INTEGER NOT NULL DEFAULT 0
    CHECK (filled_count >= 0);

CREATE INDEX idx_jobs_status ON jobs(status);

UPDATE jobs SET status = 'OPEN' WHERE is_active = true;
UPDATE jobs SET status = 'CLOSED' WHERE is_active = false;

COMMENT ON COLUMN jobs.status IS 'Job lifecycle status: OPEN (actively accepting applications), CLOSED (no longer accepting applications), HIRED (all positions filled)';
COMMENT ON COLUMN jobs.filled_count IS 'Number of applicants who have been hired for this job';
