package com.atsanalyzer.resume_ats_analyzer.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    @GetMapping("/test")
    public String test() {
        return "Auth API is working!";
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        System.out.println("📝 Register request: " + userData);
        
        try {
            String name = userData.get("name");
            String email = userData.get("email");
            String password = userData.get("password");
            
            if (name == null || email == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "All fields are required"));
            }
            
            // Success response
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("status", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        System.out.println("🔐 Login attempt: " + credentials.get("email"));
        
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password required"));
            }
            
            // Mock successful login
            Map<String, Object> response = new HashMap<>();
            response.put("token", "token-" + System.currentTimeMillis());
            response.put("userId", 1);
            response.put("email", email);
            response.put("name", email.split("@")[0]);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}