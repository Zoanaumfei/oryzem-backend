package com.oryzem.backend.config.aws;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String region = "us-east-1";
    private Credentials credentials = new Credentials();

    @Data
    public static class Credentials {
        private String accessKey;
        private String secretKey;
        private String profile;  // Para usar ~/.aws/credentials
    }
}
