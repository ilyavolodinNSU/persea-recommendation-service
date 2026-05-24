package ru.persea.recommendationservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.persea.recommendationservice.dto.PopularProductDto;
import ru.persea.recommendationservice.dto.UserProfileDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserActionRepository {

    private final JdbcTemplate jdbc;

    @Value("${app.feed.action-weights.like}") private double likeWeight;
    @Value("${app.feed.action-weights.scan}") private double scanWeight;
    @Value("${app.feed.action-weights.view}") private double viewWeight;

    public Map<String, UserProfileDto> fetchUserProfiles() {
        String sql = """
            SELECT
                ua.keycloak_id::text,
                ua.product_id,
                SUM(
                    CASE uat.name
                        WHEN 'like' THEN ?
                        WHEN 'scan' THEN ?
                        WHEN 'view' THEN ?
                        ELSE 0
                    END
                ) AS weighted_score
            FROM user_actions ua
            JOIN user_action_types uat ON uat.id = ua.type_id
            WHERE ua.created_at >= NOW() - INTERVAL '90 days'
            GROUP BY ua.keycloak_id, ua.product_id
            """;

        Map<String, UserProfileDto> profiles = new HashMap<>();


        jdbc.query(sql, rs -> {
            String userId  = rs.getString("keycloak_id");
            long productId = rs.getLong("product_id");
            double score   = rs.getDouble("weighted_score");

            profiles.computeIfAbsent(userId, UserProfileDto::new)
                    .productScores()
                    .put(productId, score);

        }, likeWeight, scanWeight, viewWeight);

        log.info("[repository] загружено {} профилей юзеров", profiles.size());
        return profiles;
    }

    public List<PopularProductDto> fetchPopularProducts(int limit) {
        String sql = """
            SELECT
                p.id AS product_id,
                (p.rating / 100.0 * 0.3)
                + (COUNT(ua.id) FILTER (
                    WHERE uat.name = 'like'
                    AND ua.created_at >= NOW() - INTERVAL '30 days'
                ) * 0.7
                ) AS popularity_score
            FROM products p
            LEFT JOIN user_actions ua       ON ua.product_id = p.id
            LEFT JOIN user_action_types uat ON uat.id = ua.type_id
            GROUP BY p.id, p.rating
            ORDER BY popularity_score DESC
            LIMIT ?
            """;

        return jdbc.query(sql, (rs, rowNum) -> new PopularProductDto(
            rs.getLong("product_id"),
            rs.getDouble("popularity_score")
        ), limit);
    }
}