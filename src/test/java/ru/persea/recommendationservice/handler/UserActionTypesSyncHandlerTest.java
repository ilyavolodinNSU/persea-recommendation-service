package ru.persea.recommendationservice.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.persea.recommendationservice.handler.dto.UserActionTypesSyncDto;
import ru.persea.recommendationservice.service.UserActionService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActionTypesSyncHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserActionService userActionService;

    @InjectMocks
    private UserActionTypesSyncHandler handler;

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
        UserActionTypesSyncDto dto = new UserActionTypesSyncDto(1L, "view");
        when(objectMapper.convertValue(afterNode, UserActionTypesSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).insertUserActionType(dto);
        verify(userActionService, never()).deleteUserActionType(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForUpdate() {
        JsonNode message = createMessage("u", afterNode, null);
        UserActionTypesSyncDto dto = new UserActionTypesSyncDto(2L, "like");
        when(objectMapper.convertValue(afterNode, UserActionTypesSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).insertUserActionType(dto);
        verify(userActionService, never()).deleteUserActionType(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForRead() {
        JsonNode message = createMessage("r", afterNode, null);
        UserActionTypesSyncDto dto = new UserActionTypesSyncDto(3L, "scan");
        when(objectMapper.convertValue(afterNode, UserActionTypesSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).insertUserActionType(dto);
        verify(userActionService, never()).deleteUserActionType(any());
    }

    @Test
    void consumeProductSync_shouldCallDeleteForDelete() {
        JsonNode message = createMessage("d", null, beforeNode);
        UserActionTypesSyncDto dto = new UserActionTypesSyncDto(4L, "purchase");
        when(objectMapper.convertValue(beforeNode, UserActionTypesSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(userActionService).deleteUserActionType(dto);
        verify(userActionService, never()).insertUserActionType(any());
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