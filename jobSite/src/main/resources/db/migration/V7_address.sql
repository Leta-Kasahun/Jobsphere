CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    city VARCHAR(100),
    sub_city VARCHAR(100),
    street VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_addresses_city ON addresses(city);