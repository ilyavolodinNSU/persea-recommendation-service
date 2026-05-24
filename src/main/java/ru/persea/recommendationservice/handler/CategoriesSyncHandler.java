package ru.persea.recommendationservice.handler;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.service.DefaultFeedService;
import ru.persea.recommendationservice.service.ProductService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CategoriesSyncHandler {

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final DefaultFeedService defaultFeedService;

    @KafkaListener(
        topics = "${kafka.topics.product-categories-cdc}",
        groupId = "${kafka.consumer-groups.product-categories-sync}",
        containerFactory = "jsonNodeKafkaListenerContainerFactory"
    )
    public void consumeProductSync(JsonNode message) {
        String op = message.get("payload").get("op").asString();
        JsonNode after  = message.get("payload").get("after");
        JsonNode before = message.get("payload").get("before");

        switch (op) {
            case "c", "r", "u" -> {
                var dto = objectMapper.convertValue(after, CategoryDto.class);
                productService.insertCategory(dto);
            }
            case "d" -> {
                var dto = objectMapper.convertValue(before, CategoryDto.class);
                productService.deleteCategory(dto);
            }
        }

        defaultFeedService.invalidateCategoryCache();
    }
}