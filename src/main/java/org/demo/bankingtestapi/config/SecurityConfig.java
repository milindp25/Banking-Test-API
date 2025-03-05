package org.demo.bankingtestapi.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);


    private final JwtEncoder jwtEncoder;
    private final SecurityProperties securityProperties;

    public SecurityConfig(JwtEncoder jwtEncoder, SecurityProperties securityProperties) {
        this.jwtEncoder = jwtEncoder;
        this.securityProperties = securityProperties;
    }

    /**
     * Actuator Security Filter Chain (Excludes JWT Authentication)
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)  // Ensures this is processed first
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Applying Actuator Security Filter Chain...");
        http
                .securityMatcher("/actuator/**")  // Apply only to actuator endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll())  // Fully allow Actuator endpoints
                .csrf(csrf -> csrf.disable())  // Disable CSRF for Actuator
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * Main API Security Filter Chain
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)  // This runs after the Actuator security configuration
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"JWT is required for this endpoint\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied. You don't have permission to access this resource.\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()  // ✅ Allow Auth APIs
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // ✅ Allow Swagger
                        .requestMatchers("/actuator/**").permitAll()  // ✅ Allow Actuator
                        .requestMatchers(securityProperties.getPublicEndpoints().toArray(new String[0])).permitAll() // Public routes
                        .requestMatchers(securityProperties.getUser().toArray(new String[0])).hasAuthority("ROLE_USER") // User role
                        .requestMatchers(securityProperties.getAdmin().toArray(new String[0])).hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT") // Admin & Agent
                        .requestMatchers(securityProperties.getAgent().toArray(new String[0])).hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT") // Admin & Agent
                        .anyRequest().authenticated() // Secure all other endpoints
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return authenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String secretKey = System.getenv("JWT_SECRET");
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("JWT_SECRET is not set!");
        }
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256")).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
