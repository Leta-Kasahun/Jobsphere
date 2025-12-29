package com.jobsphere.jobsite.service.shared;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
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
public class CloudinaryFileService {
    private final Cloudinary cloudinary;

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    private static final String[] ALLOWED_DOCUMENT_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    private static final String[] ALLOWED_VIDEO_TYPES = {
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm"
    };

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB

    /**
     * Uploads an image to Cloudinary and returns the secure URL.
     * 
     * @param file   The image file to upload
     * @param folder The folder path in Cloudinary (e.g., "seekers/profile")
     * @return The secure URL of the uploaded image
     * @throws IllegalArgumentException if file validation fails
     * @throws IOException              if upload fails
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateImageFile(file);

        // Cloudinary automatically appends the folder if specified in params,
        // so we don't need to prefix it in the public_id manually.
        String publicId = UUID.randomUUID().toString();

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "overwrite", true,
                "transformation",
                new Transformation().width(500).height(500).crop("limit").quality("auto").fetchFormat("auto"));

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                uploadParams);
        String secureUrl = (String) uploadResult.get("secure_url");

        log.info("Image uploaded successfully to Cloudinary: {}", secureUrl);
        return secureUrl;
    }

    /**
     * Uploads a PDF document to Cloudinary and returns the secure URL.
     * 
     * @param file   The PDF file to upload
     * @param folder The folder path in Cloudinary (e.g., "seekers/cv")
     * @return The secure URL of the uploaded document
     * @throws IllegalArgumentException if file validation fails
     * @throws IOException              if upload fails
     */
    public String uploadDocument(MultipartFile file, String folder) throws IOException {
        validateDocumentFile(file);

        // Cloudinary automatically appends the folder if specified in params
        String publicId = UUID.randomUUID().toString();

        // Use "auto" to let Cloudinary detect the best resource type (likely "image"
        // for PDF to allow previews)
        // or "raw" for generic files. For PDFs, "auto" usually works well.
        String resourceType = "auto";
        if (file.getContentType() != null && file.getContentType().equalsIgnoreCase("application/pdf")) {
            // Forcing 'image' for PDF allows page-by-page transformation/preview,
            // but for simple storage/retrieval 'auto' is fine.
            // Keeping 'image' logic if that was the intent, but removing the folder prefix
            // is key.
            resourceType = "image";
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", resourceType,
                "overwrite", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                uploadParams);
        String secureUrl = (String) uploadResult.get("secure_url");

        log.info("Document uploaded successfully to Cloudinary: {}", secureUrl);
        return secureUrl;
    }

    /**
     * Uploads a video to Cloudinary and returns the secure URL.
     * 
     * @param file   The video file to upload
     * @param folder The folder path in Cloudinary (e.g., "seekers/projects/videos")
     * @return The secure URL of the uploaded video
     * @throws IllegalArgumentException if file validation fails
     * @throws IOException              if upload fails
     */
    public String uploadVideo(MultipartFile file, String folder) throws IOException {
        validateVideoFile(file);

        String publicId = UUID.randomUUID().toString();

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "video",
                "overwrite", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                uploadParams);
        String secureUrl = (String) uploadResult.get("secure_url");

        log.info("Video uploaded successfully to Cloudinary: {}", secureUrl);
        return secureUrl;
    }

    /**
     * Generates an authenticated (signed) URL for a file.
     * Useful for accessing files in restricted folders or resolving 401 errors.
     *
     * @param url The original file URL
     * @return The signed URL
     */
    public String generateAuthenticatedUrl(String url) {
        if (url == null || url.isEmpty())
            return null;

        String publicId = extractPublicIdFromUrl(url);
        String version = extractVersionFromUrl(url);

        log.info("Generating Signed URL. Input: {}", url);
        log.info("Extracted PublicID: {}", publicId);
        log.info("Extracted Version: {}", version);

        if (publicId == null)
            return url;

        String format = "pdf"; // Default to pdf for these docs
        if (url.toLowerCase().endsWith(".png"))
            format = "png";
        else if (url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".jpeg"))
            format = "jpg";

        try {
            com.cloudinary.Url urlBuilder = cloudinary.url()
                    .type("authenticated")
                    .format(format)
                    .secure(true)
                    .signed(true);

            if (version != null) {
                urlBuilder.version(version);
            }

            String finalUrl = urlBuilder.generate(publicId);
            log.info("Generated Signed URL: {}", finalUrl);
            return finalUrl;
        } catch (Exception e) {
            log.error("Failed to generate signed URL for {}", url, e);
            return url;
        }
    }

    /**
     * Deletes a file from Cloudinary using its URL.
     * 
     * @param fileUrl The Cloudinary URL of the file to delete
     * @throws IOException if deletion fails
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract public_id from Cloudinary URL
            String publicId = extractPublicIdFromUrl(fileUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("File deleted successfully from Cloudinary: {}", publicId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete file from Cloudinary: {}", fileUrl, e);
            // Don't throw exception - deletion failure shouldn't break the flow
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
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPEG, PNG, GIF, WEBP");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum limit of 5MB");
        }
    }

    /**
     * Validates the document file for size and content type.
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedDocumentType(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed type: PDF");
        }

        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum limit of 10MB");
        }
    }

    private boolean isAllowedImageType(String contentType) {
        for (String allowed : ALLOWED_IMAGE_TYPES) {
            if (allowed.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedDocumentType(String contentType) {
        for (String allowed : ALLOWED_DOCUMENT_TYPES) {
            if (allowed.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the video file for size and content type.
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedVideoType(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: MP4, MPEG, MOV, AVI, WEBM");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum limit of 100MB");
        }
    }

    private boolean isAllowedVideoType(String contentType) {
        for (String allowed : ALLOWED_VIDEO_TYPES) {
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

            // Remove version prefix if present
            if (path.matches("^v\\d+/.*")) {
                path = path.substring(path.indexOf('/') + 1);
            }

            // Remove extension from last segment
            int lastDot = path.lastIndexOf('.');
            int lastSlash = path.lastIndexOf('/');
            if (lastDot > lastSlash) {
                path = path.substring(0, lastDot);
            }

            return path;
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", url, e);
            return null;
        }
    }

    private String extractVersionFromUrl(String url) {
        if (url == null)
            return null;
        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1)
                return null;
            String path = url.substring(uploadIndex + "/upload/".length());
            if (path.matches("^v\\d+/.*")) {
                String versionPart = path.substring(0, path.indexOf('/'));
                return versionPart.substring(1); // Remove 'v'
            }
        } catch (Exception e) {
        }
        return null;
    }
}
