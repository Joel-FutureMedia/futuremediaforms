package com.futuremedia.futureclientformapi.controllers;

import com.futuremedia.futureclientformapi.dto.auth.AuthResponse;
import com.futuremedia.futureclientformapi.dto.auth.LoginRequest;
import com.futuremedia.futureclientformapi.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
