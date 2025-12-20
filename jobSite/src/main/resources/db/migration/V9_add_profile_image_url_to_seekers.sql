
ALTER TABLE seekers 
ADD COLUMN profile_image_url TEXT;


COMMENT ON COLUMN seekers.profile_image_url IS 'URL of the seeker profile image stored in Cloudinary';

