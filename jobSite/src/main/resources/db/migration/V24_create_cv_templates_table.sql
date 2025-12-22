CREATE TABLE cv_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    description TEXT,
    sections JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_cv_templates_category ON cv_templates(category);
CREATE INDEX idx_cv_templates_status ON cv_templates(status);

COMMENT ON TABLE cv_templates IS 'CV templates created by admins to define structure for seeker CVs';
COMMENT ON COLUMN cv_templates.sections IS 'JSONB structure defining template sections and field requirements';
