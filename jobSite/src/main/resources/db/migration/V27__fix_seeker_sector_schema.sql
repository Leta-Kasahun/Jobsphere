ALTER TABLE seeker_sector DROP CONSTRAINT seeker_sector_pkey;

ALTER TABLE seeker_sector ADD COLUMN id UUID DEFAULT gen_random_uuid();

ALTER TABLE seeker_sector ADD PRIMARY KEY (id);

-- Ensure seeker_id is still NOT NULL (it was implied by PK, but explicit is safer after dropping PK)
ALTER TABLE seeker_sector ALTER COLUMN seeker_id SET NOT NULL;

-- Verification comment: The index idx_seeker_sector_seeker was already created in V11, so we don't need to recreate it.
