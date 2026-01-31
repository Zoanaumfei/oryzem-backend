package com.oryzem.backend.infrastructure.aws;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String region = "us-east-1";
    private String accessKeyId;
    private String secretAccessKey;
    private String profile;  // Para usar ~/.aws/credentials

}

