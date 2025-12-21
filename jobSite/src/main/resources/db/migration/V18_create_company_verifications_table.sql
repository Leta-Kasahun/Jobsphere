-- Create company_verifications table for employer verification workflow
CREATE TABLE company_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    trade_license_url TEXT NOT NULL,
    tin_number VARCHAR(50),
    website VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'BANNED')),
    verification_code CHAR(6),
    code_used BOOLEAN DEFAULT FALSE,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add index on user_id for performance
CREATE INDEX idx_company_verifications_user_id ON company_verifications(user_id);

-- Add index on status for filtering
CREATE INDEX idx_company_verifications_status ON company_verifications(status);

-- Add unique constraint on user_id to ensure one verification per user
CREATE UNIQUE INDEX idx_company_verifications_user_id_unique ON company_verifications(user_id) WHERE status IN ('PENDING', 'APPROVED');
