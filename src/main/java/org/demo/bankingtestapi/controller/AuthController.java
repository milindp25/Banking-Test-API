package org.demo.bankingtestapi.controller;

import org.demo.bankingtestapi.config.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/token")
    public Map<String, String> generateToken(@RequestParam String clientId) {
        // Assign default role as "USER" (modify as needed)
        Set<String> roles = Set.of("USER");

        // Generate a JWT token with roles
        String token = jwtUtil.generateToken(clientId, roles);
        return Map.of("access_token", token);
    }
}


