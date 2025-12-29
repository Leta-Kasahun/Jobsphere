-- Add missing job lifecycle and trust columns
ALTER TABLE jobs 
ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS payment_verified BOOLEAN DEFAULT FALSE;

-- Add indexes for filtering
CREATE INDEX IF NOT EXISTS idx_jobs_is_featured ON jobs(is_featured);
CREATE INDEX IF NOT EXISTS idx_jobs_payment_verified ON jobs(payment_verified);

-- Update existing records to false if they were null
UPDATE jobs SET is_featured = FALSE WHERE is_featured IS NULL;
UPDATE jobs SET payment_verified = FALSE WHERE payment_verified IS NULL;
