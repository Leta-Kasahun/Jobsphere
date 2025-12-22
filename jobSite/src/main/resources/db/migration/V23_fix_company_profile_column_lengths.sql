-- Migration to fix column lengths for company profiles
-- Ensure long content can be stored without "value too long" errors

ALTER TABLE company_profiles 
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN logo_url TYPE TEXT,
    ALTER COLUMN social_links TYPE TEXT,
    ALTER COLUMN industry TYPE TEXT,
    ALTER COLUMN location TYPE TEXT,
    ALTER COLUMN legal_status TYPE VARCHAR(255);
