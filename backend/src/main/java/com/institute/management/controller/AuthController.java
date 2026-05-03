package com.institute.management.controller;

import com.institute.management.dto.ApiResponse;
import com.institute.management.dto.JwtResponse;
import com.institute.management.dto.LoginRequest;
import com.institute.management.dto.RegisterRequest;
import com.institute.management.entity.User;
import com.institute.management.service.AuthService;
import com.institute.management.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.login(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid email or password"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = authService.register(registerRequest);
            
            // Send welcome email
            emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFirstName(),
                user.getRole().name()
            );
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "User registered successfully",
                user
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}