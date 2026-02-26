package com.oryzem.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Profile("dev")
    SecurityFilterChain filterChainDev(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/integrations/ifood/webhook").permitAll()
                        .requestMatchers("/api/v1/projects/**", "/api/v1/birthdays/**")
                        .hasAnyAuthority("Admin-User", "Internal-User")
                        .requestMatchers("/api/v1/items/**", "/api/v1/files/**")
                        .hasAnyAuthority("Admin-User", "Internal-User", "External-User")
                        .requestMatchers("/api/health/**")
                        .hasAnyAuthority("Admin-User", "Internal-User")
                        .requestMatchers("/admin/**").hasAuthority("Admin-User")
                        .requestMatchers("/internal/**").hasAuthority("Internal-User")
                        .requestMatchers("/external/**").hasAuthority("External-User")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                );

        return http.build();
    }

    @Bean
    @Profile("!dev")
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/integrations/ifood/webhook").permitAll()
                        .requestMatchers("/api/v1/projects/**", "/api/v1/birthdays/**")
                        .hasAnyAuthority("Admin-User", "Internal-User")
                        .requestMatchers("/api/v1/items/**", "/api/v1/files/**")
                        .hasAnyAuthority("Admin-User", "Internal-User", "External-User")
                        .requestMatchers("/api/health/**")
                        .hasAnyAuthority("Admin-User", "Internal-User")
                        .requestMatchers("/admin/**").hasAuthority("Admin-User")
                        .requestMatchers("/internal/**").hasAuthority("Internal-User")
                        .requestMatchers("/external/**").hasAuthority("External-User")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                );

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("cognito:groups");
        converter.setAuthorityPrefix(""); // IMPORTANTE

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}

