-- Create company_profiles table for employer profiles
CREATE TABLE company_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    logo_url TEXT,
    website VARCHAR(255),
    location VARCHAR(255),
    industry VARCHAR(255),
    legal_status VARCHAR(50),
    social_links TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add unique constraint to ensure one profile per user
CREATE UNIQUE INDEX idx_company_profiles_user_id_unique ON company_profiles(user_id);

-- Add index for performance
CREATE INDEX idx_company_profiles_user_id ON company_profiles(user_id);
