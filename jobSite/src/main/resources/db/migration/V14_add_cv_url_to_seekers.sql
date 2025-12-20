ALTER TABLE seekers 
ADD COLUMN cv_url TEXT;

COMMENT ON COLUMN seekers.cv_url IS 'URL of the seeker CV stored in Cloudinary';

