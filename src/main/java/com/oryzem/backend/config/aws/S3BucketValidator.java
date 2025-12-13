package com.oryzem.backend.config.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Component
public class S3BucketValidator {

    private static final Logger logger = LoggerFactory.getLogger(S3BucketValidator.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3BucketValidator(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateBucket() {
        try {
            // Verifica se o bucket existe
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());

            logger.info("Bucket S3 '{}' validado com sucesso", bucketName);

        } catch (NoSuchBucketException e) {
            logger.warn("Bucket S3 '{}' não existe. Criando...", bucketName);
            createBucket();
        } catch (Exception e) {
            logger.error("Erro ao validar bucket S3 '{}': {}", bucketName, e.getMessage());
            throw new RuntimeException("Falha na configuração do S3", e);
        }
    }

    private void createBucket() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());

            logger.info("Bucket S3 '{}' criado com sucesso", bucketName);

        } catch (BucketAlreadyExistsException e) {
            logger.info("Bucket S3 '{}' já existe", bucketName);
        } catch (Exception e) {
            logger.error("Erro ao criar bucket S3: {}", e.getMessage());
            throw new RuntimeException("Não foi possível criar o bucket S3", e);
        }
    }
}