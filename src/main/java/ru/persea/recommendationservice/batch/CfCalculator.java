package ru.persea.recommendationservice.batch;

import ru.persea.recommendationservice.dto.FeedItemResponse;
import ru.persea.recommendationservice.dto.PopularProductDto;
import ru.persea.recommendationservice.dto.UserProfileDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CfCalculator {

    @Value("${app.feed.size}")
    private int feedSize;

    @Value("${app.feed.cf.min-similarity}")
    private double minSimilarity;

    @Value("${app.feed.cf.max-neighbors}")
    private int maxNeighbors;

    public Map<String, Map<Long, FeedItemResponse>> computeCfScores(
        Map<String, UserProfileDto> profiles
    ) {

        Map<Long, List<String>> productToUsers = new HashMap<>();
        for (var entry : profiles.entrySet()) {
            for (long productId : entry.getValue().productScores().keySet()) {
                productToUsers
                    .computeIfAbsent(productId, k -> new ArrayList<>())
                    .add(entry.getKey());
            }
        }

        Map<String, Map<Long, FeedItemResponse>> result = new HashMap<>();

        for (var entry : profiles.entrySet()) {
            String userId = entry.getKey();
            Set<Long> userProducts = entry.getValue().productScores().keySet();


            Set<String> candidateNeighbors = new HashSet<>();
            for (long productId : userProducts) {
                candidateNeighbors.addAll(
                    productToUsers.getOrDefault(productId, List.of())
                );
            }
            candidateNeighbors.remove(userId);


            Map<String, Double> neighborSimilarity = new HashMap<>();
            for (String neighborId : candidateNeighbors) {
                Set<Long> neighborProducts = profiles.get(neighborId).productScores().keySet();

                long intersection = userProducts.stream()
                    .filter(neighborProducts::contains)
                    .count();
                long union = userProducts.size() + neighborProducts.size() - intersection;

                double jaccard = union > 0 ? (double) intersection / union : 0.0;

                if (jaccard >= minSimilarity) {
                    neighborSimilarity.put(neighborId, jaccard);
                }
            }


            List<Map.Entry<String, Double>> topNeighbors = neighborSimilarity.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(maxNeighbors)
                .toList();


            Map<Long, Double> candidateScores = new HashMap<>();
            for (var neighbor : topNeighbors) {
                double similarity = neighbor.getValue();

                profiles.get(neighbor.getKey()).productScores().forEach((productId, score) -> {
                    if (!userProducts.contains(productId)) {
                        candidateScores.merge(productId, similarity * score, Double::sum);
                    }
                });
            }


            Map<Long, FeedItemResponse> cfItems = new HashMap<>();
            candidateScores.forEach((productId, score) ->
                cfItems.put(productId, new FeedItemResponse(productId, score, "cf"))
            );

            result.put(userId, cfItems);
        }

        return result;
    }

    public Map<String, List<FeedItemResponse>> buildFeeds(
        Map<String, Map<Long, FeedItemResponse>> cfScores,
        List<PopularProductDto> popularProducts,
        Collection<String> allUserIds
    ) {
        Map<String, List<FeedItemResponse>> feeds = new HashMap<>();

        for (String userId : allUserIds) {
            Map<Long, FeedItemResponse> feed = new LinkedHashMap<>(
                cfScores.getOrDefault(userId, Map.of())
            );


            if (feed.size() < feedSize) {
                for (PopularProductDto popular : popularProducts) {
                    if (feed.size() >= feedSize) break;
                    feed.computeIfAbsent(
                        popular.productId(),
                        id -> new FeedItemResponse(id, popular.score(), "popular")
                    );
                }
            }


            List<FeedItemResponse> sorted = feed.values().stream()
                .sorted(Comparator.comparingDouble(FeedItemResponse::score).reversed())
                .limit(feedSize)
                .toList();

            feeds.put(userId, sorted);
        }

        return feeds;
    }
}