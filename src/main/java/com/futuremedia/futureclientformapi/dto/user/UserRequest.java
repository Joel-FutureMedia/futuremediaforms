package com.futuremedia.futureclientformapi.dto.user;

import com.futuremedia.futureclientformapi.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull Role role,
        boolean isLocked
) {}
