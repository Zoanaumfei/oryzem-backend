package com.oryzem.backend.security;

import com.oryzem.backend.core.tenant.TenantContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN_USER = "Admin-User";
    private static final String INTERNAL_USER = "Internal-User";
    private static final String EXTERNAL_USER = "External-User";
    private final TenantContextFilter tenantContextFilter;

    public SecurityConfig(TenantContextFilter tenantContextFilter) {
        this.tenantContextFilter = tenantContextFilter;
    }

    @Bean
    @Profile("dev")
    SecurityFilterChain filterChainDev(HttpSecurity http) throws Exception {
        return buildFilterChain(http);
    }

    @Bean
    @Profile("!dev")
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return buildFilterChain(http);
    }

    private SecurityFilterChain buildFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(
                                "/api/integrations/ifood/webhook",
                                "/api/integrations/rappi/webhook"
                        ).permitAll()
                        .requestMatchers(
                                "/api/orders/**",
                                "/api/inventory/**",
                                "/api/catalog/**",
                                "/api/integrations/**",
                                "/api/v1/initiatives/**"
                        ).hasAnyAuthority(ADMIN_USER, INTERNAL_USER)
                        .requestMatchers("/api/v1/projects/**", "/api/v1/birthdays/**")
                        .hasAnyAuthority(ADMIN_USER, INTERNAL_USER)
                        .requestMatchers("/api/v1/items/**", "/api/v1/files/**")
                        .hasAnyAuthority(ADMIN_USER, INTERNAL_USER, EXTERNAL_USER)
                        .requestMatchers("/api/health/**")
                        .hasAnyAuthority(ADMIN_USER, INTERNAL_USER)
                        .requestMatchers("/admin/**").hasAuthority(ADMIN_USER)
                        .requestMatchers("/internal/**").hasAuthority(INTERNAL_USER)
                        .requestMatchers("/external/**").hasAuthority(EXTERNAL_USER)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                )
                .addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class);

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

