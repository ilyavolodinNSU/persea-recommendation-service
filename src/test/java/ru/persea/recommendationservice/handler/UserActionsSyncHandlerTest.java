package ru.persea.recommendationservice.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.persea.recommendationservice.handler.dto.UserActionsSyncDto;
import ru.persea.recommendationservice.service.UserActionService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActionsSyncHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserActionService userActionService;

    @InjectMocks
    private UserActionsSyncHandler handler;

    private JsonNode afterNode;
    private JsonNode beforeNode;

    @BeforeEach
    void setUp() {
        afterNode = mock(JsonNode.class);
        beforeNode = mock(JsonNode.class);
    }

    @Test
    void consumeProductSync_shouldCallInsertForCreate() {
        JsonNode message = createMessage("c", afterNode, null);
        UserActionsSyncDto dto = new UserActionsSyncDto(1L, UUID.randomUUID(), 10L, 100L, Instant.now());
        when(objectMapper.convertValue(afterNode, UserActionsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).insertUserAction(dto);
        verify(userActionService, never()).deleteUserAction(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForUpdate() {
        JsonNode message = createMessage("u", afterNode, null);
        UserActionsSyncDto dto = new UserActionsSyncDto(2L, UUID.randomUUID(), 20L, 200L, Instant.now());
        when(objectMapper.convertValue(afterNode, UserActionsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).insertUserAction(dto);
        verify(userActionService, never()).deleteUserAction(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForRead() {
        JsonNode message = createMessage("r", afterNode, null);
        UserActionsSyncDto dto = new UserActionsSyncDto(3L, UUID.randomUUID(), 30L, 300L, Instant.now());
        when(objectMapper.convertValue(afterNode, UserActionsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).insertUserAction(dto);
        verify(userActionService, never()).deleteUserAction(any());
    }

    @Test
    void consumeProductSync_shouldCallDeleteForDelete() {
        JsonNode message = createMessage("d", null, beforeNode);
        UserActionsSyncDto dto = new UserActionsSyncDto(4L, UUID.randomUUID(), 40L, 400L, Instant.now());
        when(objectMapper.convertValue(beforeNode, UserActionsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).deleteUserAction(dto);
        verify(userActionService, never()).insertUserAction(any());
    }

    @Test
    void consumeProductSync_nullMessage_shouldDoNothing() {
        handler.consumeProductSync(null);
        verifyNoInteractions(userActionService);
    }

    @Test
    void consumeProductSync_emptyMessage_shouldDoNothing() {
        JsonNode empty = mock(JsonNode.class);
        when(empty.isEmpty()).thenReturn(true);
        handler.consumeProductSync(empty);
        verifyNoInteractions(userActionService);
    }

    private JsonNode createMessage(String op, JsonNode after, JsonNode before) {
        JsonNode payload = mock(JsonNode.class);
        JsonNode opNode = mock(JsonNode.class);
        when(opNode.asString()).thenReturn(op);
        when(payload.get("op")).thenReturn(opNode);
        if (after != null) {
            when(payload.get("after")).thenReturn(after);
        } else {
            when(payload.get("after")).thenReturn(null);
        }
        if (before != null) {
            when(payload.get("before")).thenReturn(before);
        } else {
            when(payload.get("before")).thenReturn(null);
        }

        JsonNode message = mock(JsonNode.class);
        when(message.isNull()).thenReturn(false);
        when(message.isEmpty()).thenReturn(false);
        when(message.get("payload")).thenReturn(payload);
        return message;
    }
}