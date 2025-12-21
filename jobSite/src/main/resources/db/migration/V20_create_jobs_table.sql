-- Create jobs table for employer job postings
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_profile_id UUID NOT NULL REFERENCES company_profiles(id) ON DELETE CASCADE,
    address_id UUID REFERENCES addresses(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    workplace_type VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    education_level VARCHAR(100) NOT NULL,
    gender_requirement VARCHAR(20),
    vacancy_count INTEGER,
    experience_level VARCHAR(100),
    experience_description TEXT,
    salary_min DECIMAL(15,2),
    salary_max DECIMAL(15,2),
    deadline DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add indexes for performance
CREATE INDEX idx_jobs_company_profile_id ON jobs(company_profile_id);
CREATE INDEX idx_jobs_address_id ON jobs(address_id);
CREATE INDEX idx_jobs_category ON jobs(category);
CREATE INDEX idx_jobs_job_type ON jobs(job_type);
CREATE INDEX idx_jobs_workplace_type ON jobs(workplace_type);
CREATE INDEX idx_jobs_is_active ON jobs(is_active);
CREATE INDEX idx_jobs_created_at ON jobs(created_at DESC);
