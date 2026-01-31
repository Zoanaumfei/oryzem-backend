package com.oryzem.backend.infrastructure.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;

import static software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.*;

@Configuration
public class AwsCredentialsProviderConfig {

    private final AwsProperties awsProperties;

    public AwsCredentialsProviderConfig(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    /**
     * Bean centralizado para credenciais AWS
     * Pode ser usado por DynamoDB, S3, SQS, etc.
     */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        // Prioridade de resolução de credenciais:
        // 1. Credenciais explícitas no application.yml
        if (awsProperties.getAccessKeyId() != null &&
                !awsProperties.getAccessKeyId().isBlank() &&
                awsProperties.getSecretAccessKey() != null &&
                !awsProperties.getSecretAccessKey().isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            awsProperties.getAccessKeyId(),
                            awsProperties.getSecretAccessKey()
                    )
            );
        }

        // 2. Profile específico do arquivo ~/.aws/credentials
        if (awsProperties.getProfile() != null) {
            return ProfileCredentialsProvider.create(
                    awsProperties.getProfile()
            );
        }

        // 3. Credenciais padrão da AWS (Environment, EC2 IAM Role, etc.)
        return DefaultCredentialsProvider.builder().build();
    }

    /**
     * Bean para região AWS
     */
    @Bean
    public Region awsRegion() {
        return Region.of(awsProperties.getRegion());
    }
}

