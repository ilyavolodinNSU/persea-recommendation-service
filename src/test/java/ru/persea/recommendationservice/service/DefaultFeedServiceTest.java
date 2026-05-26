package ru.persea.recommendationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.dto.ProductDto;
import ru.persea.recommendationservice.repository.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFeedServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StringRedisTemplate redis;

    @InjectMocks
    private DefaultFeedService defaultFeedService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(defaultFeedService, "minRating", 70);
        ReflectionTestUtils.setField(defaultFeedService, "categoryCount", 3);
        ReflectionTestUtils.setField(defaultFeedService, "feedSize", 10);
        ReflectionTestUtils.setField(defaultFeedService, "defaultFeedTtlSeconds", 3600L);
    }

    @Test
    void generateAndCache_shouldReturnProductIdsAndWriteToRedis() {
        String keycloakId = "user1";
        List<Long> allCategoryIds = List.of(1L, 2L, 3L, 4L, 5L);
        when(productRepository.findAllCategoryIds()).thenReturn(allCategoryIds);

        ProductDto product1 = new ProductDto(101L, "Product1", new BrandDto(1L, "Brand1"),
                new CategoryDto(1L, "Cat1", "code1"), 80, "img1");
        ProductDto product2 = new ProductDto(102L, "Product2", new BrandDto(2L, "Brand2"),
                new CategoryDto(2L, "Cat2", "code2"), 90, "img2");
        // заменяем anyList() на any()
        when(productRepository.findDefaultFeedProducts(eq(70), any(), eq(10)))
                .thenReturn(List.of(product1, product2));

        when(redis.executePipelined(any(RedisCallback.class))).thenReturn(null);

        List<Long> result = defaultFeedService.generateAndCache(keycloakId);

        assertThat(result).containsExactly(102L, 101L);
        verify(productRepository).findAllCategoryIds();
        verify(productRepository).findDefaultFeedProducts(eq(70), any(), eq(10));
        verify(redis).executePipelined(any(RedisCallback.class));
    }

    @Test
    void generateAndCache_noCategories_shouldReturnEmptyList() {
        when(productRepository.findAllCategoryIds()).thenReturn(List.of());

        List<Long> result = defaultFeedService.generateAndCache("user2");

        assertThat(result).isEmpty();
        verify(productRepository, never()).findDefaultFeedProducts(anyInt(), any(), anyInt());
        verify(redis, never()).executePipelined(any(RedisCallback.class));
    }

    @Test
    void generateAndCache_noProductsFound_shouldReturnEmptyList() {
        when(productRepository.findAllCategoryIds()).thenReturn(List.of(1L));
        when(productRepository.findDefaultFeedProducts(eq(70), any(), eq(10)))
                .thenReturn(List.of());

        List<Long> result = defaultFeedService.generateAndCache("user3");

        assertThat(result).isEmpty();
        verify(redis, never()).executePipelined(any(RedisCallback.class));
    }

    @Test
    void invalidateCategoryCache_shouldResetCachedCategoryIds() {
        // первый вызов загружает категории
        when(productRepository.findAllCategoryIds()).thenReturn(List.of(1L, 2L));
        when(productRepository.findDefaultFeedProducts(eq(70), any(), eq(10)))
                .thenReturn(List.of()); // пустой список, чтобы не писать в Redis
        defaultFeedService.generateAndCache("user4");
        verify(productRepository, times(1)).findAllCategoryIds();

        // инвалидируем кеш
        defaultFeedService.invalidateCategoryCache();

        // второй вызов должен снова загрузить категории
        // используем реальный ProductDto вместо мока
        ProductDto product = new ProductDto(1L, "p", new BrandDto(1L, "b"),
                new CategoryDto(1L, "c", "cd"), 80, "img");
        when(productRepository.findDefaultFeedProducts(eq(70), any(), eq(10)))
                .thenReturn(List.of(product));
        when(redis.executePipelined(any(RedisCallback.class))).thenReturn(null);
        defaultFeedService.generateAndCache("user5");

        verify(productRepository, times(2)).findAllCategoryIds();
    }
}