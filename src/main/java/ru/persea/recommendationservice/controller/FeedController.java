package ru.persea.recommendationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.persea.recommendationservice.batch.RecommendationJob;
import ru.persea.recommendationservice.dto.ProductDto;
import ru.persea.recommendationservice.service.FeedService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
public class FeedController {

    private final RecommendationJob job;
    private final FeedService feedService;

    @PostMapping("/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> recalculate() {
        log.info("[controller] ручной запуск пересчёта лент");
        CompletableFuture.runAsync(job::run);
        return ResponseEntity.accepted().body("recalculation started");
    }

    @GetMapping("/feed/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDto>> getUserFeed(
        @PathVariable String keycloakId,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(feedService.getFeed(keycloakId, offset, limit));
    }

    @GetMapping("/feed/me")
    public ResponseEntity<List<ProductDto>> getMyFeed(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "20") int limit,
        JwtAuthenticationToken jwt
    ) {
        return ResponseEntity.ok(feedService.getFeed(jwt.getToken().getSubject(), offset, limit));
    }
}