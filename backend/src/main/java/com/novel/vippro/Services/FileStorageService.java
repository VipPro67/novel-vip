package com.novel.vippro.Services;

import java.io.IOException;

/**
 * Abstraction over file storage providers so different backends (Cloudinary, S3, Azure Blob, etc.) can be used.
 */
public interface FileStorageService {

    String uploadFile(byte[] fileData, String publicId, String contentType) throws IOException;

    byte[] downloadFile(String publicId) throws IOException;

    void deleteFile(String publicId) throws IOException;
}
