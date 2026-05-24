package ru.persea.recommendationservice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.persea.recommendationservice.dto.ProductDto;
import ru.persea.recommendationservice.repository.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultFeedService {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redis;

    @Value("${app.feed.default.min-rating:70}")
    private int minRating;

    @Value("${app.feed.default.category-count:5}")
    private int categoryCount;

    @Value("${app.feed.size}")
    private int feedSize;

    @Value("${app.feed.default.ttl-seconds:3600}")
    private long defaultFeedTtlSeconds;

    private volatile List<Long> cachedCategoryIds = null;

    private record ScoredProduct(ProductDto product, double score) {}

    public List<Long> generateAndCache(String keycloakId) {
        log.info("[default-feed] генерация для {}", keycloakId);

        List<Long> allCategoryIds = getAllCategoryIds();
        if (allCategoryIds.isEmpty()) {
            log.warn("[default-feed] нет категорий в БД");
            return List.of();
        }

        List<Long> selectedCategories = pickRandomCategories(allCategoryIds);

        List<ProductDto> products = productRepository.findDefaultFeedProducts(
            minRating, selectedCategories, feedSize
        );

        if (products.isEmpty()) {
            log.warn("[default-feed] нет продуктов rating>={} в категориях {}", minRating, selectedCategories);
            return List.of();
        }

        List<ScoredProduct> scored = assignScores(products);
        writeToRedis(keycloakId, scored);

        return scored.stream()
            .map(sp -> sp.product().id())
            .toList();
    }

    public void invalidateCategoryCache() {
        cachedCategoryIds = null;
        log.info("[default-feed] кеш категорий сброшен");
    }

    private List<Long> getAllCategoryIds() {
        if (cachedCategoryIds == null) {
            synchronized (this) {
                if (cachedCategoryIds == null) {
                    cachedCategoryIds = productRepository.findAllCategoryIds();
                    log.info("[default-feed] загружено {} категорий", cachedCategoryIds.size());
                }
            }
        }
        return cachedCategoryIds;
    }

    private List<Long> pickRandomCategories(List<Long> allCategoryIds) {
        List<Long> shuffled = new ArrayList<>(allCategoryIds);
        Collections.shuffle(shuffled, new Random(ThreadLocalRandom.current().nextLong()));
        int count = Math.min(categoryCount, shuffled.size());
        return shuffled.subList(0, count);
    }

    private List<ScoredProduct> assignScores(List<ProductDto> products) {
        return products.stream()
            .map(p -> new ScoredProduct(
                p,
                (p.rating() / 100.0) + ThreadLocalRandom.current().nextDouble(0.0, 0.1)
            ))
            .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed())
            .toList();
    }

    private void writeToRedis(String keycloakId, List<ScoredProduct> scored) {
        if (scored.isEmpty()) return;

        String feedKey    = "feed:"          + keycloakId;
        String metaKey    = "feed_meta:"     + keycloakId;
        String tmpFeedKey = "feed:tmp:"      + keycloakId;
        String tmpMetaKey = "feed_meta:tmp:" + keycloakId;

        redis.executePipelined((RedisCallback<Object>) connection -> {
            connection.keyCommands().del(
                tmpFeedKey.getBytes(), tmpMetaKey.getBytes()
            );

            for (ScoredProduct sp : scored) {
                connection.zSetCommands().zAdd(
                    tmpFeedKey.getBytes(),
                    sp.score(),
                    String.valueOf(sp.product().id()).getBytes()
                );
                connection.hashCommands().hSet(
                    tmpMetaKey.getBytes(),
                    String.valueOf(sp.product().id()).getBytes(),
                    "default".getBytes()
                );
            }

            connection.keyCommands().expire(tmpFeedKey.getBytes(), defaultFeedTtlSeconds);
            connection.keyCommands().expire(tmpMetaKey.getBytes(), defaultFeedTtlSeconds);
            connection.keyCommands().rename(tmpFeedKey.getBytes(), feedKey.getBytes());
            connection.keyCommands().rename(tmpMetaKey.getBytes(), metaKey.getBytes());

            return null;
        });

        log.info("[default-feed] записано {} продуктов для {} (TTL={}s)",
            scored.size(), keycloakId, defaultFeedTtlSeconds);
    }
}
