package com.futuremedia.futureclientformapi.dto.auth;

import com.futuremedia.futureclientformapi.models.Role;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        Role role
) {}
