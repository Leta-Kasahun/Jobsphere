-- Migration to add compensation and currency to jobs
ALTER TABLE jobs
    ADD COLUMN compensation_type VARCHAR(50),
    ADD COLUMN currency VARCHAR(10);
