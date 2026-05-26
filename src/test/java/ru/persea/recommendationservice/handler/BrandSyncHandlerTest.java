package ru.persea.recommendationservice.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.persea.recommendationservice.dto.BrandDto;
import ru.persea.recommendationservice.service.ProductService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandSyncHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private BrandSyncHandler handler;

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
        BrandDto dto = new BrandDto(1L, "Brand");
        when(objectMapper.convertValue(afterNode, BrandDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertBrand(dto);
        verify(productService, never()).deleteBrand(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForUpdate() {
        JsonNode message = createMessage("u", afterNode, null);
        BrandDto dto = new BrandDto(2L, "Updated");
        when(objectMapper.convertValue(afterNode, BrandDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertBrand(dto);
        verify(productService, never()).deleteBrand(any());
    }

    @Test
    void consumeProductSync_shouldCallInsertForRead() {
        JsonNode message = createMessage("r", afterNode, null);
        BrandDto dto = new BrandDto(3L, "Snapshot");
        when(objectMapper.convertValue(afterNode, BrandDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertBrand(dto);
        verify(productService, never()).deleteBrand(any());
    }

    @Test
    void consumeProductSync_shouldCallDeleteForDelete() {
        JsonNode message = createMessage("d", null, beforeNode);
        BrandDto dto = new BrandDto(4L, "Deleted");
        when(objectMapper.convertValue(beforeNode, BrandDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).deleteBrand(dto);
        verify(productService, never()).insertBrand(any());
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
        when(opNode.asString()).thenReturn(op);            // <-- заменили asText() на asString()
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