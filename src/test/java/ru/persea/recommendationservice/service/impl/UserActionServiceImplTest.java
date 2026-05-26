package ru.persea.recommendationservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.persea.recommendationservice.handler.dto.UserActionsSyncDto;
import ru.persea.recommendationservice.handler.dto.UserActionTypesSyncDto;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class UserActionServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserActionServiceImpl userActionService;

    // ---------- UserAction tests ----------
    @Test
    void insertUserAction_shouldExecuteUpsert() {
        Instant now = Instant.now();
        UserActionsSyncDto dto = new UserActionsSyncDto(
                1L, UUID.randomUUID(), 10L, 100L, now
        );

        userActionService.insertUserAction(dto);

        verify(jdbcTemplate).update(
                eq("INSERT INTO user_actions (id, keycloak_id, product_id, type_id, created_at) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET keycloak_id = EXCLUDED.keycloak_id, " +
                        "product_id = EXCLUDED.product_id, type_id = EXCLUDED.type_id, created_at = EXCLUDED.created_at"),
                eq(1L), eq(dto.keycloakId()), eq(10L), eq(100L), eq(Timestamp.from(now))
        );
    }

    @Test
    void updateUserAction_shouldExecuteUpdate() {
        Instant now = Instant.now();
        UserActionsSyncDto dto = new UserActionsSyncDto(
                2L, UUID.randomUUID(), 20L, 200L, now
        );

        userActionService.updateUserAction(dto);

        verify(jdbcTemplate).update(
                eq("UPDATE user_actions SET keycloak_id = ?, product_id = ?, type_id = ?, created_at = ? WHERE id = ?"),
                eq(dto.keycloakId()), eq(20L), eq(200L), eq(Timestamp.from(now)), eq(2L)
        );
    }

    @Test
    void deleteUserAction_shouldExecuteDelete() {
        UserActionsSyncDto dto = new UserActionsSyncDto(
                3L, null, null, null, null
        );

        userActionService.deleteUserAction(dto);

        verify(jdbcTemplate).update("DELETE FROM user_actions WHERE id = ?", 3L);
    }

    // ---------- UserActionType tests ----------
    @Test
    void insertUserActionType_shouldExecuteUpsert() {
        UserActionTypesSyncDto dto = new UserActionTypesSyncDto(1L, "view");

        userActionService.insertUserActionType(dto);

        verify(jdbcTemplate).update(
                eq("INSERT INTO user_action_types (id, name) VALUES (?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name"),
                eq(1L), eq("view")
        );
    }

    @Test
    void updateUserActionType_shouldExecuteUpdate() {
        UserActionTypesSyncDto dto = new UserActionTypesSyncDto(2L, "purchase");

        userActionService.updateUserActionType(dto);

        verify(jdbcTemplate).update(
                eq("UPDATE user_action_types SET name = ? WHERE id = ?"),
                eq("purchase"), eq(2L)
        );
    }

    @Test
    void deleteUserActionType_shouldExecuteDelete() {
        UserActionTypesSyncDto dto = new UserActionTypesSyncDto(3L, "any");

        userActionService.deleteUserActionType(dto);

        verify(jdbcTemplate).update("DELETE FROM user_action_types WHERE id = ?", 3L);
    }
}