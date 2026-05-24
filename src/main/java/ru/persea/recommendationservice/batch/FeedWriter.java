package ru.persea.recommendationservice.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.persea.recommendationservice.dto.FeedItemResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedWriter {

    private final StringRedisTemplate redis;

    @Value("${app.feed.ttl-seconds}")
    private long ttlSeconds;

    public void write(Map<String, List<FeedItemResponse>> feeds) {
        redis.executePipelined((RedisCallback<Object>) connection -> {
            for (var entry : feeds.entrySet()) {
                String userId = entry.getKey();
                List<FeedItemResponse> items = entry.getValue();

                String feedKey    = "feed:" + userId;
                String metaKey    = "feed_meta:" + userId;
                String tmpFeedKey = "feed:tmp:" + userId;
                String tmpMetaKey = "feed_meta:tmp:" + userId;


                connection.keyCommands().del(
                    tmpFeedKey.getBytes(),
                    tmpMetaKey.getBytes()
                );

                if (items.isEmpty()) {

                    connection.keyCommands().del(
                        feedKey.getBytes(),
                        metaKey.getBytes()
                    );
                    continue;
                }


                for (FeedItemResponse item : items) {
                    connection.zSetCommands().zAdd(
                        tmpFeedKey.getBytes(),
                        item.score(),
                        String.valueOf(item.productId()).getBytes()
                    );
                }

                Map<byte[], byte[]> metaMap = new java.util.HashMap<>();
                for (FeedItemResponse item : items) {
                    metaMap.put(
                        String.valueOf(item.productId()).getBytes(),
                        item.reason().getBytes()
                    );
                }
                connection.hashCommands().hMSet(tmpMetaKey.getBytes(), metaMap);


                connection.keyCommands().expire(tmpFeedKey.getBytes(), ttlSeconds);
                connection.keyCommands().expire(tmpMetaKey.getBytes(), ttlSeconds);


                connection.keyCommands().rename(tmpFeedKey.getBytes(), feedKey.getBytes());
                connection.keyCommands().rename(tmpMetaKey.getBytes(), metaKey.getBytes());
            }

            return null;
        });

        log.info("[writer] записано {} лент в Redis", feeds.size());
    }
}