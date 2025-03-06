package org.demo.bankingtestapi.controller;

import jakarta.validation.Valid;
import org.demo.bankingtestapi.DTO.RegisterUserDto;
import org.demo.bankingtestapi.config.JwtUtil;
import org.demo.bankingtestapi.entity.User;
import org.demo.bankingtestapi.service.AuditService;
import org.demo.bankingtestapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuditService auditService;

    public AuthController(JwtUtil jwtUtil, UserService userService, AuditService auditService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.auditService = auditService;
    }

    @PostMapping("/token")
    public Map<String, String> generateToken(@RequestParam String clientId) {
        // For demonstration, using a fixed role. Adjust as needed.
        String roles = "ADMIN";
        String token = jwtUtil.generateToken(clientId, roles);
        return Map.of("access_token", token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestHeader(name = "X-Client-ID", required = false) String clientId,
            @Valid @RequestBody RegisterUserDto registerUserDto) {

        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Client ID is required.");
        }

        // Convert LocalDate to java.sql.Date
        Date dob = Date.valueOf(registerUserDto.getDateOfBirth());

        User user = userService.registerUser(
                registerUserDto.getUsername(),
                registerUserDto.getEmail(),
                registerUserDto.getPassword(),
                registerUserDto.getPhoneNumber(),
                registerUserDto.getNationalId(),
                registerUserDto.getAddress(),
                dob,
                registerUserDto.getZipCode(),
                registerUserDto.getRoleId()
        );

        // Log registration event in the audit table
        auditService.logAction(user, "User registration successful for email: " + user.getEmail());

        return ResponseEntity.ok(Map.of("message", "User registered successfully", "userId", user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        return userService.findByUserName(username)
                .map(user -> {
                    if (user.getPasswordHash() == null) {
                        return ResponseEntity.status(403)
                                .body(Map.of("error", "Password not set. Please reset your password."));
                    }

                    if (!userService.validatePassword(password, user.getPasswordHash())) {
                        // Log failed login attempt
                        auditService.logAction(user, "Failed login attempt due to invalid credentials.");
                        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
                    }

                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
                    // Log successful login event
                    auditService.logAction(user, "User login successful.");
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

        // Optionally log the password reset action if the user is found
        userService.findByUserName(email).ifPresent(user ->
                auditService.logAction(user, "Password reset successfully for user: " + email)
        );

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    private boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
