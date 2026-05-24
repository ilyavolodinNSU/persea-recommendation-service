package ru.persea.recommendationservice.handler;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.persea.recommendationservice.handler.dto.ProductsSyncDto;
import ru.persea.recommendationservice.service.ProductService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ProductsSyncHandler {

    private final ObjectMapper objectMapper;
    private final ProductService productService;

    @KafkaListener(
        topics = "${kafka.topics.products-cdc}",
        groupId = "${kafka.consumer-groups.products-sync}",
        containerFactory = "jsonNodeKafkaListenerContainerFactory"
    )
    public void consumeProductSync(JsonNode message) {
        String op = message.get("payload").get("op").asString();
        JsonNode after = message.get("payload").get("after");
        JsonNode before = message.get("payload").get("before");

        switch (op) {
            case "c", "r", "u" -> {
                var dto = objectMapper.convertValue(after, ProductsSyncDto.class);
                productService.insertProduct(dto);
            }
            case "d" -> {
                var dto = objectMapper.convertValue(before, ProductsSyncDto.class);
                productService.deleteProduct(dto);
            }
        }
    }
}