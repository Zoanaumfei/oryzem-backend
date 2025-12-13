package com.oryzem.backend.services.s3;

import com.oryzem.backend.config.aws.S3BucketProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3BucketProperties bucketProperties;

    public S3StorageService(S3Client s3Client,
                            S3Presigner s3Presigner,
                            S3BucketProperties bucketProperties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketProperties = bucketProperties;
    }

    /**
     * Upload de arquivo
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String key = generateKey(file.getOriginalFilename(), folder);

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketProperties.getBucketName())
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        return key;
    }

    /**
     * Gerar URL pré-assinada para download
     */
    public URL generatePresignedUrl(String key, Duration expiration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketProperties.getBucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url();
    }

    /**
     * Upload de arquivo público
     */
    public String uploadPublicFile(MultipartFile file, String folder) throws IOException {
        String key = generateKey(file.getOriginalFilename(), folder);

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketProperties.getBucketName())
                        .key(key)
                        .contentType(file.getContentType())
                        .acl(ObjectCannedACL.PUBLIC_READ)  // Arquivo público
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        return bucketProperties.getObjectUrl(key);
    }

    /**
     * Gerar chave única para arquivo
     */
    private String generateKey(String originalFilename, String folder) {
        String uuid = UUID.randomUUID().toString();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        if (folder != null && !folder.trim().isEmpty()) {
            return String.format("%s/%s%s", folder.trim(), uuid, extension);
        }

        return String.format("%s%s", uuid, extension);
    }

    /**
     * Listar objetos no bucket
     */
    public ListObjectsV2Response listObjects(String prefix) {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketProperties.getBucketName())
                .prefix(prefix)
                .build());
    }

    /**
     * Deletar objeto
     */
    public void deleteObject(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketProperties.getBucketName())
                .key(key)
                .build());
    }
}