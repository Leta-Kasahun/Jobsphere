ALTER TABLE seeker_cv ALTER COLUMN about TYPE VARCHAR(2000);
COMMENT ON COLUMN seeker_cv.about IS 'Stores a short bio or professional summary extracted from the CV builder';
