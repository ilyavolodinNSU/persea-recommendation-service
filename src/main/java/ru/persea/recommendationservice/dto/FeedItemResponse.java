package ru.persea.recommendationservice.dto;

public record FeedItemResponse(
    long productId,
    double score,
    String reason
) {}
