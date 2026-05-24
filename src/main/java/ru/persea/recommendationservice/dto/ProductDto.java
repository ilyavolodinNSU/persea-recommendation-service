package ru.persea.recommendationservice.dto;

public record ProductDto(
    Long id,
    String name, 
    BrandDto brand,
    CategoryDto category,
    Integer rating,
    String imageURI
) {}
