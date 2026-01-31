package com.oryzem.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class CorsConfig {

    private final Environment environment;
    private final CorsProperties corsProperties;

    public CorsConfig(Environment environment, CorsProperties corsProperties) {
        this.environment = environment;
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                Set<String> allowedOrigins = new LinkedHashSet<>(corsProperties.allowedOrigins());
                if (environment.acceptsProfiles(Profiles.of("dev"))) {
                    allowedOrigins.addAll(corsProperties.devAllowedOrigins());
                }

                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.toArray(new String[0]))
                        .allowedMethods(corsProperties.allowedMethods().toArray(new String[0]))
                        .allowedHeaders(corsProperties.allowedHeaders().toArray(new String[0]))
                        .allowCredentials(corsProperties.allowCredentials())
                        .maxAge(corsProperties.maxAge());
            }
        };
    }
}
