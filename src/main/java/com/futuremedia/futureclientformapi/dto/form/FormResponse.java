package com.futuremedia.futureclientformapi.dto.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.futuremedia.futureclientformapi.models.FormStatus;

import java.time.Instant;

public record FormResponse(
        Long id,
        Long userId,
        String ownerName,
        String companyName,
        String companyEmail,
        String contactPerson,
        JsonNode formPayload,
        FormStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
