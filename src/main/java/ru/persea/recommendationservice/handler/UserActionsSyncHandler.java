package ru.persea.recommendationservice.handler;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.persea.recommendationservice.handler.dto.UserActionsSyncDto;
import ru.persea.recommendationservice.service.UserActionService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class UserActionsSyncHandler {

    private final ObjectMapper objectMapper;
    private final UserActionService userActionService;

    @KafkaListener(
        topics = "${kafka.topics.user-actions-cdc}",
        groupId = "${kafka.consumer-groups.user-actions-sync}",
        containerFactory = "jsonNodeKafkaListenerContainerFactory"
    )
    public void consumeProductSync(@Payload(required = false) JsonNode message) {
        if (message == null || message.isNull() || message.isEmpty()) return;

        String op = message.get("payload").get("op").asString();
        JsonNode after = message.get("payload").get("after");
        JsonNode before = message.get("payload").get("before");

        switch (op) {
            case "c", "r", "u" -> {
                var dto = objectMapper.convertValue(after, UserActionsSyncDto.class);
                userActionService.insertUserAction(dto);
            }
            case "d" -> {
                var dto = objectMapper.convertValue(before, UserActionsSyncDto.class);
                userActionService.deleteUserAction(dto);
            }
        }
    }
}