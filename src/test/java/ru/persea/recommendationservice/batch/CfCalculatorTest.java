package ru.persea.recommendationservice.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.persea.recommendationservice.dto.FeedItemResponse;
import ru.persea.recommendationservice.dto.PopularProductDto;
import ru.persea.recommendationservice.dto.UserProfileDto;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CfCalculatorTest {

    private final CfCalculator calculator = new CfCalculator();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(calculator, "feedSize", 10);
        ReflectionTestUtils.setField(calculator, "minSimilarity", 0.2);
        ReflectionTestUtils.setField(calculator, "maxNeighbors", 2);
    }

    @Test
    void computeCfScores_shouldCalculateCorrectly() {
        UserProfileDto user1 = new UserProfileDto("user1");
        user1.productScores().putAll(Map.of(101L, 3.0, 102L, 1.0));
        UserProfileDto user2 = new UserProfileDto("user2");
        user2.productScores().putAll(Map.of(101L, 2.0, 103L, 2.0));
        UserProfileDto user3 = new UserProfileDto("user3");
        user3.productScores().putAll(Map.of(104L, 1.0));

        Map<String, UserProfileDto> profiles = Map.of("user1", user1, "user2", user2, "user3", user3);

        Map<String, Map<Long, FeedItemResponse>> result = calculator.computeCfScores(profiles);

        assertThat(result).containsKeys("user1", "user2", "user3");
        assertThat(result.get("user1")).containsKey(103L);
        assertThat(result.get("user1").get(103L).score()).isCloseTo(0.6667, within(0.01));
        assertThat(result.get("user2")).containsKey(102L);
        assertThat(result.get("user2").get(102L).score()).isCloseTo(0.3333, within(0.01));
        assertThat(result.get("user3")).isEmpty();
    }

    @Test
    void buildFeeds_shouldFillWithPopularIfLessThanFeedSize() {
        Map<String, Map<Long, FeedItemResponse>> cfScores = Map.of(
                "user1", Map.of(101L, new FeedItemResponse(101L, 0.8, "cf"))
        );
        List<PopularProductDto> popular = List.of(
                new PopularProductDto(201L, 0.9), new PopularProductDto(202L, 0.7)
        );

        var feeds = calculator.buildFeeds(cfScores, popular, List.of("user1"));
        assertThat(feeds.get("user1")).hasSize(3);
        assertThat(feeds.get("user1").get(0).productId()).isEqualTo(201L); // score 0.9
        assertThat(feeds.get("user1").get(1).productId()).isEqualTo(101L); // 0.8
        assertThat(feeds.get("user1").get(2).productId()).isEqualTo(202L); // 0.7
    }
}