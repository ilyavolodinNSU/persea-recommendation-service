package ru.persea.recommendationservice.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.persea.recommendationservice.handler.dto.ProductsSyncDto;
import ru.persea.recommendationservice.service.ProductService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsSyncHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductsSyncHandler handler;

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
        ProductsSyncDto dto = new ProductsSyncDto(1L, "Prod", 10L, 20L, 5, "img", 1_700_000_000_000L);
        when(objectMapper.convertValue(afterNode, ProductsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertProduct(dto);
        verify(productService, never()).deleteProduct(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForUpdate() {
        JsonNode message = createMessage("u", afterNode, null);
        ProductsSyncDto dto = new ProductsSyncDto(2L, "Upd", 11L, 21L, 4, "img2", 1_700_000_000_001L);
        when(objectMapper.convertValue(afterNode, ProductsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertProduct(dto);
        verify(productService, never()).deleteProduct(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForRead() {
        JsonNode message = createMessage("r", afterNode, null);
        ProductsSyncDto dto = new ProductsSyncDto(3L, "Snap", 12L, 22L, 3, "img3", 1_700_000_000_002L);
        when(objectMapper.convertValue(afterNode, ProductsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertProduct(dto);
        verify(productService, never()).deleteProduct(any());
    }

    @Test
    void consumeProductSync_shouldCallDeleteForDelete() {
        JsonNode message = createMessage("d", null, beforeNode);
        ProductsSyncDto dto = new ProductsSyncDto(4L, "Del", 13L, 23L, 2, "img4", 1_700_000_000_003L);
        when(objectMapper.convertValue(beforeNode, ProductsSyncDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).deleteProduct(dto);
        verify(productService, never()).insertProduct(any());
    }

    @Test
    void consumeProductSync_nullMessage_shouldDoNothing() {
        handler.consumeProductSync(null);
        verifyNoInteractions(productService);
    }

    @Test
    void consumeProductSync_emptyMessage_shouldDoNothing() {
        JsonNode empty = mock(JsonNode.class);
        when(empty.isEmpty()).thenReturn(true);
        handler.consumeProductSync(empty);
        verifyNoInteractions(productService);
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