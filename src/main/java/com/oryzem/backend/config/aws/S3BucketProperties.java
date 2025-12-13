package com.oryzem.backend.config.aws;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class S3BucketProperties {
    private String bucketName;
    private String multipartUploadThreshold;
    private int defaultPresignerExpirationMinutes;
    private String region;

    // Métodos utilitários
    public String getObjectUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }
}