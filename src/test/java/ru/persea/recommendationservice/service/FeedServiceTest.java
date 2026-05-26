package ru.persea.recommendationservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.dto.ProductDto;
import ru.persea.recommendationservice.repository.ProductRepository;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DefaultFeedService defaultFeedService;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private FeedService feedService;

    @Test
    void getFeed_dataInRedis_shouldReturnWithoutGeneration() {
        String keycloakId = "user1";
        String feedKey = "feed:user1";

        when(redis.opsForZSet()).thenReturn(zSetOperations);
        Set<ZSetOperations.TypedTuple<String>> items = new LinkedHashSet<>();
        items.add(tuple("101", 0.9));
        items.add(tuple("102", 0.8));
        when(zSetOperations.reverseRangeWithScores(feedKey, 0L, 1L)).thenReturn(items);

        ProductDto product1 = new ProductDto(101L, "P1", new BrandDto(1L, "B1"),
                new CategoryDto(1L, "C1", "c1"), 80, "img");
        ProductDto product2 = new ProductDto(102L, "P2", new BrandDto(2L, "B2"),
                new CategoryDto(2L, "C2", "c2"), 90, "img");
        when(productRepository.findAllByIds(argThat(list ->
                list.containsAll(List.of(101L, 102L))))).thenReturn(List.of(product1, product2));

        List<ProductDto> result = feedService.getFeed(keycloakId, 0, 2);

        assertThat(result).containsExactly(product1, product2);
        verify(defaultFeedService, never()).generateAndCache(any());
    }

    @Test
    void getFeed_keyExistsButNoDataInRange_shouldReturnEmpty() {
        String keycloakId = "user2";
        String feedKey = "feed:user2";

        when(redis.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(feedKey, 0L, 0L)).thenReturn(Collections.emptySet());
        when(redis.hasKey(feedKey)).thenReturn(true);

        List<ProductDto> result = feedService.getFeed(keycloakId, 0, 1);

        assertThat(result).isEmpty();
        verify(defaultFeedService, never()).generateAndCache(any());
    }

    @Test
    void getFeed_coldStart_shouldGenerateAndReturn() {
        String keycloakId = "user3";
        String feedKey = "feed:user3";

        when(redis.opsForZSet()).thenReturn(zSetOperations);

        // Первый вызов reverseRangeWithScores вернёт пустой Set, второй – сгенерированные элементы
        Set<ZSetOperations.TypedTuple<String>> generatedItems = new LinkedHashSet<>();
        generatedItems.add(tuple("201", 0.95));
        generatedItems.add(tuple("202", 0.85));

        when(zSetOperations.reverseRangeWithScores(feedKey, 0L, 1L))
                .thenReturn(Collections.emptySet())   // первый вызов
                .thenReturn(generatedItems);         // второй вызов

        when(redis.hasKey(feedKey)).thenReturn(false);
        when(defaultFeedService.generateAndCache(keycloakId)).thenReturn(List.of(201L, 202L));

        ProductDto product201 = new ProductDto(201L, "GP1", new BrandDto(3L, "B3"),
                new CategoryDto(3L, "C3", "c3"), 85, "img");
        ProductDto product202 = new ProductDto(202L, "GP2", new BrandDto(4L, "B4"),
                new CategoryDto(4L, "C4", "c4"), 88, "img");
        when(productRepository.findAllByIds(argThat(list ->
                list.containsAll(List.of(201L, 202L))))).thenReturn(List.of(product201, product202));

        List<ProductDto> result = feedService.getFeed(keycloakId, 0, 2);

        assertThat(result).containsExactly(product201, product202);
        verify(defaultFeedService).generateAndCache(keycloakId);
    }

    @Test
    void getFeed_coldStartEmptyGeneration_shouldReturnEmpty() {
        String keycloakId = "user4";
        String feedKey = "feed:user4";

        when(redis.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(feedKey, 0L, 0L)).thenReturn(Collections.emptySet());
        when(redis.hasKey(feedKey)).thenReturn(false);
        when(defaultFeedService.generateAndCache(keycloakId)).thenReturn(Collections.emptyList());

        List<ProductDto> result = feedService.getFeed(keycloakId, 0, 1);

        assertThat(result).isEmpty();
        verify(zSetOperations, times(1)).reverseRangeWithScores(anyString(), anyLong(), anyLong());
    }

    private ZSetOperations.TypedTuple<String> tuple(String value, double score) {
        ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
        lenient().when(tuple.getValue()).thenReturn(value);
        lenient().when(tuple.getScore()).thenReturn(score);
        return tuple;
    }
}