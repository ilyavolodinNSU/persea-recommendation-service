package ru.persea.recommendationservice.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.persea.recommendationservice.dto.CategoryDto;
import ru.persea.recommendationservice.service.DefaultFeedService;
import ru.persea.recommendationservice.service.ProductService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriesSyncHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductService productService;

    @Mock
    private DefaultFeedService defaultFeedService;

    @InjectMocks
    private CategoriesSyncHandler handler;

    private JsonNode afterNode;
    private JsonNode beforeNode;

    @BeforeEach
    void setUp() {
        afterNode = mock(JsonNode.class);
        beforeNode = mock(JsonNode.class);
    }

    @Test
    void consumeProductSync_shouldCallInsertAndInvalidateCacheForCreate() {
        JsonNode message = createMessage("c", afterNode, null);
        CategoryDto dto = new CategoryDto(1L, "Cat", "code");
        when(objectMapper.convertValue(afterNode, CategoryDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertCategory(dto);
        verify(productService, never()).deleteCategory(any());
        verify(defaultFeedService).invalidateCategoryCache();
    }

    @Test
    void consumeProductSync_shouldCallInsertAndInvalidateCacheForUpdate() {
        JsonNode message = createMessage("u", afterNode, null);
        CategoryDto dto = new CategoryDto(2L, "Updated", "upd");
        when(objectMapper.convertValue(afterNode, CategoryDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertCategory(dto);
        verify(productService, never()).deleteCategory(any());
        verify(defaultFeedService).invalidateCategoryCache();
    }

    @Test
    void consumeProductSync_shouldCallInsertAndInvalidateCacheForRead() {
        JsonNode message = createMessage("r", afterNode, null);
        CategoryDto dto = new CategoryDto(3L, "Snapshot", "snap");
        when(objectMapper.convertValue(afterNode, CategoryDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).insertCategory(dto);
        verify(productService, never()).deleteCategory(any());
        verify(defaultFeedService).invalidateCategoryCache();
    }

    @Test
    void consumeProductSync_shouldCallDeleteAndInvalidateCacheForDelete() {
        JsonNode message = createMessage("d", null, beforeNode);
        CategoryDto dto = new CategoryDto(4L, "Deleted", "del");
        when(objectMapper.convertValue(beforeNode, CategoryDto.class)).thenReturn(dto);

        handler.consumeProductSync(message);

        verify(productService).deleteCategory(dto);
        verify(productService, never()).insertCategory(any());
        verify(defaultFeedService).invalidateCategoryCache();
    }

    @Test
    void consumeProductSync_nullMessage_shouldDoNothing() {
        handler.consumeProductSync(null);
        verifyNoInteractions(productService, defaultFeedService);
    }

    @Test
    void consumeProductSync_emptyMessage_shouldDoNothing() {
        JsonNode empty = mock(JsonNode.class);
        when(empty.isEmpty()).thenReturn(true);
        handler.consumeProductSync(empty);
        verifyNoInteractions(productService, defaultFeedService);
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