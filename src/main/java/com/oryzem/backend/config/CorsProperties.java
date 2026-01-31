package com.oryzem.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        long maxAge,
        List<String> devAllowedOrigins
) {
    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        allowedMethods = allowedMethods == null ? List.of() : List.copyOf(allowedMethods);
        allowedHeaders = allowedHeaders == null ? List.of() : List.copyOf(allowedHeaders);
        devAllowedOrigins = devAllowedOrigins == null ? List.of() : List.copyOf(devAllowedOrigins);
    }
}
