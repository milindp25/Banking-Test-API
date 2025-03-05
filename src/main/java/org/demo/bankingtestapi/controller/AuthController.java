package org.demo.bankingtestapi.controller;

import org.demo.bankingtestapi.config.JwtUtil;
import org.demo.bankingtestapi.entity.User;
import org.demo.bankingtestapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/token")
    public Map<String, String> generateToken(@RequestParam String clientId) {
        // Assign default role as "USER" (modify as needed)
       String roles = "ADMIN";

        // Generate a JWT token with roles
        String token = jwtUtil.generateToken(clientId, roles);
        return Map.of("access_token", token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestHeader(name = "X-Client-ID", required = false) String clientId,@RequestBody Map<String, Object> request) {
        if (clientId == null || clientId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Client ID is required"));
        }
        String username = (String) request.get("username");
        String email = (String) request.get("email");
        String password = (String) request.get("password");
        String phoneNumber = (String) request.get("phoneNumber");
        String nationalId = (String) request.get("nationalId");
        String address = (String) request.get("address");
        Date dateOfBirth = new Date((long) request.get("dateOfBirth"));
        Long zipCode = Long.valueOf((Integer) request.get("zipCode"));
        String roleName = request.getOrDefault("role", "USER").toString(); // Default to USER

        User user = userService.registerUser(username, email, password, phoneNumber, nationalId, address, dateOfBirth, zipCode, roleName);
        return ResponseEntity.ok(Map.of("message", "User registered successfully", "userId", user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        return userService.findByUserName(username)
                .map(user -> {
                    if (user.getPasswordHash() == null) {
                        return ResponseEntity.status(403).body(Map.of("error", "Password not set. Please reset your password."));
                    }

                    if (!userService.validatePassword(password, user.getPasswordHash())) {
                        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
                    }

                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
                    return ResponseEntity.ok(Map.of("access_token", token, "token_type", "Bearer"));
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "User not found")));

    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (!isPasswordStrong(newPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters long, contain an uppercase letter, a lowercase letter, a number, and a special character."));
        }

        boolean success = userService.resetPassword(email, newPassword);
        if (!success) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}


