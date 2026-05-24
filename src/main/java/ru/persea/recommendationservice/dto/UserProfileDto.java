package ru.persea.recommendationservice.dto;

import java.util.HashMap;
import java.util.Map;



public record UserProfileDto(
    String keycloakId,
    Map<Long, Double> productScores
) {
    public UserProfileDto(String keycloakId) {
        this(keycloakId, new HashMap<>());
    }
}
