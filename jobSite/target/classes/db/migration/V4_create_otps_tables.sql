-- OTPs for regular users (email verification, password reset)
CREATE TABLE user_otps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(512) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET')),
    created_at TIMESTAMPTZ DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN DEFAULT false
);

-- OTPs for admin users (ALWAYS used for login)
CREATE TABLE admin_otps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID REFERENCES admin_users(id) ON DELETE CASCADE,
    code_hash VARCHAR(512) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN DEFAULT false
);

-- Indexes
CREATE INDEX idx_user_otps_user_id ON user_otps(user_id);
CREATE INDEX idx_user_otps_email ON user_otps(email);
CREATE INDEX idx_user_otps_expires ON user_otps(expires_at) WHERE NOT used;
CREATE INDEX idx_admin_otps_admin_id ON admin_otps(admin_id);
CREATE INDEX idx_admin_otps_expires ON admin_otps(expires_at) WHERE NOT used;