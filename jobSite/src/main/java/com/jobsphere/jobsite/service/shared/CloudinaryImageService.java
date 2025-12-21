package com.jobsphere.jobsite.service.shared;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImageService {
    private final Cloudinary cloudinary;

    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Uploads an image to Cloudinary and returns the secure URL.
     * 
     * @param file   The image file to upload
     * @param folder The folder path in Cloudinary (e.g., "seekers/profile")
     * @return The secure URL of the uploaded image
     * @throws IllegalArgumentException if file validation fails
     * @throws IOException              if upload fails or credentials are missing
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateImageFile(file);

        String publicId = folder + "/" + UUID.randomUUID();

        try {
            // Simplified upload parameters: transformations are removed to avoid signature
            // issues
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "resource_type", "image",
                    "overwrite", true);

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                    uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary: {}", secureUrl);
            return secureUrl;
        } catch (Exception e) {
            log.error("Cloudinary upload failed: {}. Check credentials in application.properties.", e.getMessage());
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an image from Cloudinary using its URL.
     * 
     * @param imageUrl The Cloudinary URL of the image to delete
     * @throws IOException if deletion fails
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted successfully from Cloudinary: {}", publicId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary: {}", imageUrl, e);
        }
    }

    /**
     * Validates the image file for size and content type.
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPEG, PNG, GIF, WEBP");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum limit of 5MB");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty())
            return null;

        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1)
                return null;

            String path = url.substring(uploadIndex + "/upload/".length());
            String[] segments = path.split("/");
            if (segments.length == 0)
                return null;

            StringBuilder publicId = new StringBuilder();
            for (String segment : segments) {
                if (!segment.matches("^v\\d+$") && !segment.contains("_") && !segment.contains(",")) {
                    if (publicId.length() > 0)
                        publicId.append("/");
                    String name = segment.contains(".") ? segment.substring(0, segment.lastIndexOf('.')) : segment;
                    publicId.append(name);
                }
            }
            return publicId.length() > 0 ? publicId.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", url, e);
            return null;
        }
    }
}
