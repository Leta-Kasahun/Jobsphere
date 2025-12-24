-- Create payments table for Chapa payment integration
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tx_ref VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'ETB',
    status VARCHAR(50) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    chapa_checkout_url TEXT,
    chapa_transaction_id VARCHAR(255),
    chapa_response TEXT,
    email VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    paid_at TIMESTAMP,
    verified_at TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_tx_ref ON payments(tx_ref);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);
CREATE INDEX idx_payments_user_status ON payments(user_id, status);

-- Add comment
COMMENT ON TABLE payments IS 'Stores payment transactions for job postings and other services';
