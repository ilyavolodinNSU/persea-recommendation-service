package ru.persea.recommendationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.persea.recommendationservice.batch.RecommendationJob;
import ru.persea.recommendationservice.dto.ProductDto;
import ru.persea.recommendationservice.service.FeedService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final RecommendationJob job;
    private final FeedService feedService;

    @PostMapping("/recalculate")

    public ResponseEntity<String> recalculate() {
        log.info("[controller] ручной запуск пересчёта лент");
        CompletableFuture.runAsync(job::run);
        return ResponseEntity.accepted().body("recalculation started");
    }

    @GetMapping("/{keycloakId}")
    public List<ProductDto> getFeed(
        @PathVariable String keycloakId,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return feedService.getFeed(keycloakId, offset, limit);
    }
}