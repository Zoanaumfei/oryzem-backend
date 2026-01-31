package com.oryzem.backend.infrastructure.persistence.dynamodb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.time.Duration;

@Configuration
public class DynamoDBConfig {

    @Value("${aws.dynamodb.endpoint:#{null}}")
    private String dynamoDbEndpoint;

    @Value("${aws.dynamodb-local.enabled:false}")
    private boolean dynamoDbLocalEnabled;

    @Value("${aws.dynamodb-local.url:http://localhost:8000}")
    private String dynamoDbLocalUrl;

    @Value("${aws.dynamodb.max-connections:50}")
    private int maxConnections;

    @Value("${aws.dynamodb.timeout-seconds:30}")
    private int timeoutSeconds;

    private final AwsCredentialsProvider credentialsProvider;
    private final Region region;

    public DynamoDBConfig(AwsCredentialsProvider credentialsProvider, Region region) {
        this.credentialsProvider = credentialsProvider;
        this.region = region;
    }

    @Bean
    @Profile("!test")
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .httpClientBuilder(ApacheHttpClient.builder()
                        .maxConnections(maxConnections)
                        .connectionTimeout(Duration.ofSeconds(10))
                        .socketTimeout(Duration.ofSeconds(timeoutSeconds))
                );

        // Override para DynamoDB Local
        if (dynamoDbLocalEnabled) {
            builder.endpointOverride(URI.create(dynamoDbLocalUrl));
        }
        // Override para endpoint customizado (VPC, LocalStack, etc.)
        else if (dynamoDbEndpoint != null && !dynamoDbEndpoint.trim().isEmpty()) {
            builder.endpointOverride(URI.create(dynamoDbEndpoint));
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    /**
     * Configuração específica para testes
     */
    @Bean
    @Profile("test")
    public DynamoDbClient dynamoDbClientForTest() {
        return DynamoDbClient.builder()
                .region(region)
                .endpointOverride(URI.create(dynamoDbLocalUrl))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")
                ))
                .build();
    }
}

