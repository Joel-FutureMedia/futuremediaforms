package com.futuremedia.futureclientformapi.dto.user;

import com.futuremedia.futureclientformapi.models.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean isLocked,
        Instant createdAt
) {}
