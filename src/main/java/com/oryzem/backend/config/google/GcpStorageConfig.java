package com.oryzem.backend.config.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class GcpStorageConfig {

    @Value("${GCP_PROJECT_ID:}")  // Lê da variável de ambiente
    private String projectId;

    @Value("${GCP_BUCKET_NAME:oryzem-bucket}")
    private String bucketName;

    @Bean
    public Storage storage() throws IOException {
        // 1. Pega o JSON da variável de ambiente
        String credentialsJson = System.getenv("GCP_CREDENTIALS_JSON");

        if (credentialsJson == null || credentialsJson.trim().isEmpty()) {
            throw new RuntimeException("Variável GCP_CREDENTIALS_JSON não encontrada!");
        }

        // 2. Converte para GoogleCredentials
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(credentialsJson.getBytes())
        );

        // 3. Cria o cliente do Storage
        StorageOptions.Builder builder = StorageOptions.newBuilder()
                .setCredentials(credentials);

        if (projectId != null && !projectId.isEmpty()) {
            builder.setProjectId(projectId);
        }

        return builder.build().getService();
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }
}