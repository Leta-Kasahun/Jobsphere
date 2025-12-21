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

        String publicId = folder + "/" + UUID.randomUUID();

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

        String publicId = folder + "/" + UUID.randomUUID();

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "raw",
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

        String publicId = folder + "/" + UUID.randomUUID();

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
