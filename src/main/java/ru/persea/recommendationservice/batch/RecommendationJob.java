package ru.persea.recommendationservice.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.persea.recommendationservice.dto.PopularProductDto;
import ru.persea.recommendationservice.dto.UserProfileDto;
import ru.persea.recommendationservice.repository.UserActionRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationJob {

    private final UserActionRepository repository;
    private final CfCalculator calculator;
    private final FeedWriter writer;

    @Scheduled(cron = "0 0 */2 * * *")
    public void run() {
        log.info("[job] старт пересчёта лент");

        log.info("[job] загрузка user_actions...");
        Map<String, UserProfileDto> profiles = repository.fetchUserProfiles();
        log.info("[job] загружено {} юзеров", profiles.size());

        log.info("[job] загрузка популярных продуктов...");
        List<PopularProductDto> popular = repository.fetchPopularProducts(200);
        log.info("[job] загружено {} популярных продуктов", popular.size());

        log.info("[job] расчёт CF скоров...");
        var cfScores = calculator.computeCfScores(profiles);

        log.info("[job] сборка лент...");
        var feeds = calculator.buildFeeds(cfScores, popular, profiles.keySet());

        log.info("[job] запись в Redis...");
        writer.write(feeds);

        log.info("[job] готово ✓");
    }
}