package ru.persea.recommendationservice.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.persea.recommendationservice.dto.FeedItemResponse;
import ru.persea.recommendationservice.dto.PopularProductDto;
import ru.persea.recommendationservice.dto.UserProfileDto;
import ru.persea.recommendationservice.repository.UserActionRepository;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationJobTest {

    @Mock
    private UserActionRepository repository;
    @Mock
    private CfCalculator calculator;
    @Mock
    private FeedWriter writer;

    @InjectMocks
    private RecommendationJob job;

    @Test
    void run_shouldExecuteFullPipeline() {
        Map<String, UserProfileDto> profiles = Map.of("user1", new UserProfileDto("user1"));
        List<PopularProductDto> popular = List.of(new PopularProductDto(1L, 0.9));
        Map<String, Map<Long, FeedItemResponse>> cfScores = Map.of("user1", Map.of());
        Map<String, List<FeedItemResponse>> feeds = Map.of("user1", List.of());

        when(repository.fetchUserProfiles()).thenReturn(profiles);
        when(repository.fetchPopularProducts(200)).thenReturn(popular);
        when(calculator.computeCfScores(profiles)).thenReturn(cfScores);
        when(calculator.buildFeeds(cfScores, popular, profiles.keySet())).thenReturn(feeds);

        job.run();

        verify(repository).fetchUserProfiles();
        verify(repository).fetchPopularProducts(200);
        verify(calculator).computeCfScores(profiles);
        verify(calculator).buildFeeds(cfScores, popular, profiles.keySet());
        verify(writer).write(feeds);
    }
}