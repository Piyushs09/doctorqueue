package com.doctorqueue.doctorqueue.controller;

import com.doctorqueue.doctorqueue.dto.AuthResponse;
import com.doctorqueue.doctorqueue.dto.LoginRequest;
import com.doctorqueue.doctorqueue.dto.RegisterRequest;
import com.doctorqueue.doctorqueue.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}