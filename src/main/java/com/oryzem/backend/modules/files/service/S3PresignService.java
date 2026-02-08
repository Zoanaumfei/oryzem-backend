package com.oryzem.backend.modules.files.service;

import com.oryzem.backend.modules.files.dto.PresignDownloadResponse;
import com.oryzem.backend.modules.files.dto.PresignUploadRequest;
import com.oryzem.backend.modules.files.dto.PresignUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class S3PresignService {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private static final long EXPIRES_IN_SECONDS = 600;

    private final S3Presigner presigner;
    private final String bucket;

    public S3PresignService(
            AwsCredentialsProvider credentialsProvider,
            Region region,
            @Value("${aws.s3.endpoint:}") String s3Endpoint,
            @Value("${UPLOAD_BUCKET:${app.files.upload-bucket:}}") String bucket
    ) {
        this.bucket = bucket == null ? "" : bucket.trim();
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider);

        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            builder.endpointOverride(URI.create(s3Endpoint));
        }

        this.presigner = builder.build();
    }

    public PresignUploadResponse presignUpload(PresignUploadRequest request) {
        String configuredBucket = requireConfiguredBucket();
        validateSize(request.sizeBytes());

        String sanitizedFileName = sanitizeFileName(request.originalFileName());
        String key = buildKey(sanitizedFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(configuredBucket)
                .key(key)
                .contentType(request.contentType())
                .contentLength(request.sizeBytes())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        return new PresignUploadResponse(
                key,
                presignedRequest.url().toString(),
                EXPIRES_IN_SECONDS
        );
    }

    public PresignDownloadResponse presignDownload(String key) {
        String configuredBucket = requireConfiguredBucket();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(configuredBucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

        return new PresignDownloadResponse(
                presignedRequest.url().toString(),
                EXPIRES_IN_SECONDS
        );
    }

    private void validateSize(Long sizeBytes) {
        if (sizeBytes != null && sizeBytes > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File size exceeds 10MB"
            );
        }
    }

    private String buildKey(String sanitizedFileName) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String uuid = UUID.randomUUID().toString();
        return String.format(
                "uploads/%04d/%02d/%02d/%s-%s",
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                uuid,
                sanitizedFileName
        );
    }

    private String sanitizeFileName(String originalFileName) {
        String trimmed = originalFileName == null ? "" : originalFileName.trim();
        if (trimmed.isEmpty()) {
            return "file";
        }

        StringBuilder sanitized = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '-' || ch == '_') {
                sanitized.append(ch);
            } else {
                sanitized.append('_');
            }
        }

        String result = sanitized.toString();
        if (result.length() > 120) {
            result = result.substring(0, 120);
        }
        if (result.isEmpty()) {
            return "file";
        }
        return result;
    }

    private String requireConfiguredBucket() {
        if (bucket.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "File storage is not configured (UPLOAD_BUCKET)"
            );
        }
        return bucket;
    }

    @PreDestroy
    public void shutdown() {
        presigner.close();
    }
}

