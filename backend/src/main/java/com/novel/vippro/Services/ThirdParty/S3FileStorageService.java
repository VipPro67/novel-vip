package com.novel.vippro.Services.ThirdParty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.novel.vippro.Services.FileStorageService;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;

@Service("s3FileStorageService")
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3FileStorageService.class);


    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3FileStorageService(S3Client s3Client, S3Presigner s3Presigner, @Value("${aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(byte[] fileData, String publicId, String contentType) throws IOException {
        try {
            logger.info("Uploading file to S3. publicId: {}, contentType: {}, data length: {}", publicId, contentType, fileData != null ? fileData.length : 0);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(publicId)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(fileData));
            URL url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(publicId));
            return url.toExternalForm();
        } catch (SdkException e) {
            logger.error("Failed to upload file to S3. publicId: {}", publicId, e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    @Override
    public byte[] downloadFile(String publicId) throws IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(publicId)
                    .build();
            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(request);
            return responseBytes.asByteArray();
        } catch (SdkException e) {
            logger.error("Failed to download file from S3. publicId: {}", publicId, e);
            throw new IOException("Failed to download file from S3", e);
        }
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(publicId)
                    .build();
            s3Client.deleteObject(request);
        } catch (SdkException e) {
            logger.error("Failed to delete file from S3. publicId: {}", publicId, e);
            throw new IOException("Failed to delete file from S3", e);
        }
    }

    @Override
    public String generateFileUrl(String publicId, int expirationInSeconds){
        try {
            var getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(publicId)
                    .build();

            var presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(java.time.Duration.ofSeconds(expirationInSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();

            URL presignedUrl = s3Presigner.presignGetObject(presignRequest).url();
            return presignedUrl.toExternalForm();
        } catch (SdkException e) {
            logger.error("Failed to generate presigned URL for S3 object. publicId: {}", publicId, e);
            return null;
        }
    }
}
