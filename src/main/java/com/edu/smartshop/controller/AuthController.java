package com.edu.smartshop.controller;

import com.edu.smartshop.dto.request.LoginDTO;
import com.edu.smartshop.entity.User;
import com.edu.smartshop.service.IAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginDTO loginDTO,
            HttpSession session) {
        
        User user = authService.login(loginDTO, session);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        authService.logout(session);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        User user = authService.getCurrentUser(session);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        
        return ResponseEntity.ok(response);
    }
}
