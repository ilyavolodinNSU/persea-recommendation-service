package ru.persea.recommendationservice.handler;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.persea.recommendationservice.handler.dto.UserActionTypesSyncDto;
import ru.persea.recommendationservice.service.UserActionService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class UserActionTypesSyncHandler {

    private final ObjectMapper objectMapper;
    private final UserActionService userActionService;

    @KafkaListener(
        topics = "${kafka.topics.user-action-types-cdc}",
        groupId = "${kafka.consumer-groups.user-action-types-sync}",
        containerFactory = "jsonNodeKafkaListenerContainerFactory"
    )
    public void consumeProductSync(JsonNode message) {
        String op = message.get("payload").get("op").asString();
        JsonNode after = message.get("payload").get("after");
        JsonNode before = message.get("payload").get("before");

        switch (op) {
            case "c", "r", "u" -> {
                var dto = objectMapper.convertValue(after, UserActionTypesSyncDto.class);
                userActionService.insertUserActionType(dto);
            }
            case "d" -> {
                var dto = objectMapper.convertValue(before, UserActionTypesSyncDto.class);
                userActionService.deleteUserActionType(dto);
            }
        }
    }
}