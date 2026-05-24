package ru.persea.recommendationservice.handler.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserActionsSyncDto(
    Long id,
    @JsonProperty("keycloak_id")
    UUID keycloakId,
    @JsonProperty("product_id")
    Long productId,
    @JsonProperty("type_id")
    Long typeId,
    @JsonProperty("created_at")
    Instant createdAt
) {}