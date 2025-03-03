package org.demo.bankingtestapi.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtEncoder jwtEncoder;

    public AuthController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/token")
    public Map<String, String> generateToken(@RequestParam String clientId) {
        // Define token claims
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:9000") // OAuth2 issuer
                .subject(clientId)
                .claim("scope", Set.of("read", "write"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // Token expires in 1 hour
                .build();

        // Generate JWT token
        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));

        return Map.of("access_token", jwt.getTokenValue());
    }
}

