package com.atsanalyzer.resume_ats_analyzer.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import com.atsanalyzer.resume_ats_analyzer.repository.UserRepository;
import com.atsanalyzer.resume_ats_analyzer.model.User;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
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
            
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already exists"));
            }
            
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password); // Note: storing plain text since no security dependency is present
            
            userRepository.save(user);
            
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
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
            }
            
            User user = userOpt.get();
            if (!user.getPassword().equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
            }
            
            // Successful login
            Map<String, Object> response = new HashMap<>();
            response.put("token", "token-" + System.currentTimeMillis()); // Mock token
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}