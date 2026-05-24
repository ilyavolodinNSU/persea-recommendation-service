package ru.persea.recommendationservice.dto;

public record PopularProductDto(
    long productId,
    double score
) {}
