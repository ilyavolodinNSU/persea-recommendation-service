package ru.persea.recommendationservice.handler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductsSyncDto(
    Long id,
    String name,
    @JsonProperty("brand_id")
    Long brandId,
    @JsonProperty("category_id")
    Long categoryId,
    Integer rating,
    @JsonProperty("image_uri")
    String imageUri,
    @JsonProperty("updated_at")
    Long updatedAt
) {}