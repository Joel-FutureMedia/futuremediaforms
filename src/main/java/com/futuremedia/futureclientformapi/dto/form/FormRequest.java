package com.futuremedia.futureclientformapi.dto.form;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FormRequest(
        @NotBlank String companyName,
        @Email @NotBlank String companyEmail,
        @NotBlank String contactPerson,
        @NotNull JsonNode formPayload
) {}
