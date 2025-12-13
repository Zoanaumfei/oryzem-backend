package com.oryzem.backend.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

@Configuration
public class S3Config {

    @Value("${aws.s3.endpoint:#{null}}")
    private String s3Endpoint;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.max-connections:100}")
    private int maxConnections;

    @Value("${aws.s3.multipart.threshold:5MB}")
    private String multipartThreshold;

    @Value("${aws.s3.presigner.default-expiration-minutes:15}")
    private int presignerExpirationMinutes;

    private final AwsCredentialsProvider credentialsProvider;
    private final Region region;

    public S3Config(AwsCredentialsProvider credentialsProvider, Region region) {
        this.credentialsProvider = credentialsProvider;
        this.region = region;
    }

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .httpClientBuilder(ApacheHttpClient.builder()
                        .maxConnections(maxConnections)
                        .connectionTimeout(Duration.ofSeconds(10))
                        .socketTimeout(Duration.ofSeconds(60))
                );

        // Para LocalStack ou S3-compatible storage
        if (s3Endpoint != null && !s3Endpoint.trim().isEmpty()) {
            builder.endpointOverride(URI.create(s3Endpoint));

            // Para LocalStack, desabilita algumas verificações
            if (s3Endpoint.contains("localhost") || s3Endpoint.contains("localstack")) {
                builder.serviceConfiguration(s -> s
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                );
            }
        }

        return builder.build();
    }

    /**
     * S3 Presigner para URLs pré-assinadas
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * Configurações do bucket
     */
    @Bean
    public S3BucketProperties s3BucketProperties() {
        return S3BucketProperties.builder()
                .bucketName(bucketName)
                .multipartUploadThreshold(multipartThreshold)
                .defaultPresignerExpirationMinutes(presignerExpirationMinutes)
                .build();
    }

    /**
     * Bean para validação do bucket na inicialização
     */
    @Bean
    public S3BucketValidator s3BucketValidator(S3Client s3Client, S3BucketProperties properties) {
        return new S3BucketValidator(s3Client, properties.getBucketName());
    }
}