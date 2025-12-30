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

        // ALWAYS use 'raw' for documents. This treats them as plain binary files.
        // Using 'image' for PDFs can trigger 401/404 errors due to Cloudinary's
        // default security settings restricting PDF-as-image delivery.
        String resourceType = "raw";

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }

        // For 'raw' resources, the extension MUST be part of the public_id
        // for Cloudinary to serve it with the correct Content-Type.
        String publicId = UUID.randomUUID().toString() + extension;

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

        log.info("Document uploaded successfully to Cloudinary as {}: {}", resourceType, secureUrl);
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
     * @return A signed URL
     */
    public String generateSignedUrl(String url) {
        if (url == null || url.isEmpty() || !url.contains("cloudinary.com")) {
            return url;
        }

        try {
            // Ensure we use HTTPS
            if (url.startsWith("http://")) {
                url = url.replace("http://", "https://");
            }

            // Strip existing signature: s--...--
            String cleanUrl = url.replaceFirst("/s--[^/]+--/", "/");

            String publicId = extractPublicIdFromUrl(cleanUrl);
            String version = extractVersionFromUrl(cleanUrl);

            if (publicId == null)
                return url;

            // Detect delivery type (upload, authenticated, private)
            String type = "upload";
            if (url.contains("/authenticated/"))
                type = "authenticated";
            else if (url.contains("/private/"))
                type = "private";

            // Detect resource type (image, raw, video)
            String resourceType = "image";
            if (url.contains("/raw/"))
                resourceType = "raw";
            else if (url.contains("/video/"))
                resourceType = "video";

            // Extract format (extension)
            String format = null;
            if (!"raw".equals(resourceType)) {
                int lastDot = url.lastIndexOf('.');
                int lastSlash = url.lastIndexOf('/');
                if (lastDot > lastSlash) {
                    format = url.substring(lastDot + 1);
                }
            }

            com.cloudinary.Url urlBuilder = cloudinary.url()
                    .resourceType(resourceType)
                    .type(type)
                    .signed(true)
                    .secure(true);

            if (version != null && !version.isEmpty()) {
                urlBuilder.version(version);
            }

            // IMPORTANT: For 'raw' resources, DO NOT use .format() because the
            // extension is already embedded in the publicId.
            if (format != null && !format.isEmpty() && !"raw".equals(resourceType)) {
                urlBuilder.format(format);
            }

            String signedUrl = urlBuilder.generate(publicId);
            log.debug("URL Signed: {} -> {}", url, signedUrl);
            return signedUrl;
        } catch (Exception e) {
            log.warn("Failed to generate signed URL for {}: {}", url, e.getMessage());
            return url;
        }
    }

    /**
     * Generates an authenticated (signed) URL for a file.
     * Legacy method, now uses the more robust generateSignedUrl.
     */
    public String generateAuthenticatedUrl(String url) {
        return generateSignedUrl(url);
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
            String temp = url;
            // Remove everything before delivery type segments
            String[] markers = { "/upload/", "/authenticated/", "/private/" };
            int markerIndex = -1;
            for (String marker : markers) {
                markerIndex = temp.indexOf(marker);
                if (markerIndex != -1) {
                    temp = temp.substring(markerIndex + marker.length());
                    break;
                }
            }

            if (markerIndex == -1)
                return null;

            // Strip signature if present: s--...--
            if (temp.startsWith("s--")) {
                int nextSlash = temp.indexOf('/');
                if (nextSlash != -1) {
                    temp = temp.substring(nextSlash + 1);
                }
            }

            // Strip version: v123/
            if (temp.matches("^v\\d+/.*")) {
                int nextSlash = temp.indexOf('/');
                if (nextSlash != -1) {
                    temp = temp.substring(nextSlash + 1);
                }
            }

            // Detect resource type from original URL
            boolean isRaw = url.contains("/raw/");

            // Cloudinary public_ids for images/videos in URLs DO NOT include the extension
            // unless it's a 'raw' resource where the extension IS the public_id.
            if (!isRaw) {
                int lastDot = temp.lastIndexOf('.');
                int lastSlash = temp.lastIndexOf('/');
                if (lastDot > lastSlash) {
                    temp = temp.substring(0, lastDot);
                }
            }

            return temp;
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", url, e);
            return null;
        }
    }

    private String extractVersionFromUrl(String url) {
        if (url == null || url.isEmpty())
            return null;
        try {
            String temp = url;
            String[] markers = { "/upload/", "/authenticated/", "/private/" };
            int markerIndex = -1;
            for (String marker : markers) {
                markerIndex = temp.indexOf(marker);
                if (markerIndex != -1) {
                    temp = temp.substring(markerIndex + marker.length());
                    break;
                }
            }

            if (markerIndex == -1)
                return null;

            // Skip signature if present
            if (temp.startsWith("s--")) {
                int slash = temp.indexOf('/');
                if (slash != -1)
                    temp = temp.substring(slash + 1);
            }

            if (temp.matches("^v\\d+/.*")) {
                String versionPart = temp.substring(0, temp.indexOf('/'));
                return versionPart.substring(1); // Remove 'v'
            }
        } catch (Exception e) {
        }
        return null;
    }
}
