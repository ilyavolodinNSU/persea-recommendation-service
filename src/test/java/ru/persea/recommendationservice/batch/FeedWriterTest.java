package ru.persea.recommendationservice.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.persea.recommendationservice.dto.FeedItemResponse;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedWriterTest {

    @Mock
    private StringRedisTemplate redis;

    @InjectMocks
    private FeedWriter writer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(writer, "ttlSeconds", 3600L);
    }

    @Test
    void write_shouldExecutePipelinedForNonEmptyFeed() {
        when(redis.executePipelined(any(RedisCallback.class))).thenReturn(null);

        Map<String, List<FeedItemResponse>> feeds = Map.of(
                "user1", List.of(
                        new FeedItemResponse(101L, 0.9, "cf"),
                        new FeedItemResponse(102L, 0.8, "popular")
                )
        );
        writer.write(feeds);

        verify(redis).executePipelined(any(RedisCallback.class));
    }

    @Test
    void write_emptyFeedShouldDeleteKeys() {
        when(redis.executePipelined(any(RedisCallback.class))).thenReturn(null);
        Map<String, List<FeedItemResponse>> feeds = Map.of(
                "user2", List.of()
        );
        writer.write(feeds);
        verify(redis).executePipelined(any(RedisCallback.class));
    }
}