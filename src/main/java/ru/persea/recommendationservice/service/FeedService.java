package ru.persea.recommendationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.persea.recommendationservice.dto.ProductDto;
import ru.persea.recommendationservice.repository.ProductRepository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final StringRedisTemplate redis;
    private final ProductRepository productRepository;
    private final DefaultFeedService defaultFeedService;

    public List<ProductDto> getFeed(String keycloakId, int offset, int limit) {
        String feedKey = "feed:" + keycloakId;

        List<ProductDto> fromRedis = fetchFromRedis(feedKey, offset, limit);
        if (!fromRedis.isEmpty()) {
            return fromRedis;
        }

        if (Boolean.TRUE.equals(redis.hasKey(feedKey))) {
            log.debug("[feed] offset={} вышел за границы ленты для {}", offset, keycloakId);
            return List.of();
        }

        log.info("[feed] cold start для {}", keycloakId);
        List<Long> productIds = defaultFeedService.generateAndCache(keycloakId);

        if (productIds.isEmpty()) {
            return List.of();
        }

        return fetchFromRedis(feedKey, offset, limit);
    }

    private List<ProductDto> fetchFromRedis(String feedKey, int offset, int limit) {
        Set<ZSetOperations.TypedTuple<String>> items =
            redis.opsForZSet().reverseRangeWithScores(
                feedKey,
                offset,
                (long) offset + limit - 1
            );

        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = items.stream()
            .map(item -> Long.parseLong(item.getValue()))
            .toList();

        return productRepository.findAllByIds(productIds);
    }
}