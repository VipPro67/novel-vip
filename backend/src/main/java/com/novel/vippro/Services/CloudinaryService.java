package com.novel.vippro.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(byte[] fileData, String publicId, String contentType) throws IOException {
        Map<String, String> resourceType = determineResourceType(contentType);
        logger.info("Uploading file with publicId: {}, resourceType: {}", publicId, resourceType);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(fileData, ObjectUtils.asMap(
                "resource_type", resourceType.get("resourceType"),
                "public_id", publicId));

        return (String) result.get("url");
    }

    public byte[] downloadFile(String publicId) throws IOException {
        try {
            ApiResponse apiResponse = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = apiResponse;
            String resourceType = (String) resource.get("resource_type");

            String url = cloudinary.url()
                    .resourceType(resourceType)
                    .generate(publicId);

            // Here you would implement the actual download logic
            // This is a placeholder - you'll need to implement the actual download logic
            return new byte[0];
        } catch (Exception e) {
            throw new IOException("Failed to download file from Cloudinary", e);
        }
    }

    public void deleteFile(String publicId) throws IOException {
        try {
            ApiResponse apiResponse = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            @SuppressWarnings("unchecked")
            Map<String, Object> resource = apiResponse;
            String resourceType = (String) resource.get("resource_type");

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));

            if (!(Boolean) result.get("result").equals("ok")) {
                throw new IOException("Failed to delete file from Cloudinary");
            }
        } catch (Exception e) {
            throw new IOException("Failed to delete file from Cloudinary", e);
        }
    }

    private Map<String, String> determineResourceType(String contentType) {
        if (contentType.startsWith("image/")) {
            return Map.of("resourceType", "image", "format", contentType.substring(6));
        } else if (contentType.startsWith("video/")) {
            return Map.of("resourceType", "video", "format", contentType.substring(6));
        } else if (contentType.startsWith("audio/")) {
            return Map.of("resourceType", "video", "format", contentType.substring(6));
        } else if (contentType.startsWith("application/json")) {
            return Map.of("resourceType", "raw", "format", contentType.substring(6));
        } else {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
    }

private String getSignedUrl(String publicId, String resourceType, int expirationInSeconds) {
    long expiresAt = (System.currentTimeMillis() / 1000L) + expirationInSeconds;

    return cloudinary.url()
            .resourceType(resourceType)  
            .signed(true)
            .expiresAt(expiresAt)        
            .generate(publicId);
}

}