-- Refine seeker profile: Add Tagline and Social Link updates

-- 1. Add tagline to seekers table
ALTER TABLE seekers ADD COLUMN IF NOT EXISTS tagline VARCHAR(255);

-- 2. Add updated_at to social links for better tracking
ALTER TABLE seeker_social_links ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now();

COMMENT ON COLUMN seekers.tagline IS 'Brief professional headline (LinkedIn style)';
