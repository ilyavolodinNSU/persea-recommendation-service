package ru.persea.recommendationservice.service.impl;

import java.sql.Timestamp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.persea.recommendationservice.handler.dto.UserActionsSyncDto;
import ru.persea.recommendationservice.handler.dto.UserActionTypesSyncDto;
import ru.persea.recommendationservice.service.UserActionService;

@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insertUserAction(UserActionsSyncDto dto) {
        jdbcTemplate.update(
            "INSERT INTO user_actions (id, keycloak_id, product_id, type_id, created_at) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET keycloak_id = EXCLUDED.keycloak_id, " +
            "product_id = EXCLUDED.product_id, type_id = EXCLUDED.type_id, created_at = EXCLUDED.created_at",
            dto.id(), dto.keycloakId(), dto.productId(), dto.typeId(), Timestamp.from(dto.createdAt())
        );
    }

    @Override
    public void updateUserAction(UserActionsSyncDto dto) {
        jdbcTemplate.update(
            "UPDATE user_actions SET keycloak_id = ?, product_id = ?, type_id = ?, created_at = ? WHERE id = ?",
            dto.keycloakId(), dto.productId(), dto.typeId(), Timestamp.from(dto.createdAt()), dto.id()
        );
    }

    @Override
    public void deleteUserAction(UserActionsSyncDto dto) {
        jdbcTemplate.update("DELETE FROM user_actions WHERE id = ?", dto.id());
    }

    @Override
    public void insertUserActionType(UserActionTypesSyncDto dto) {
        jdbcTemplate.update(
            "INSERT INTO user_action_types (id, name) VALUES (?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name",
            dto.id(), dto.name()
        );
    }

    @Override
    public void updateUserActionType(UserActionTypesSyncDto dto) {
        jdbcTemplate.update(
            "UPDATE user_action_types SET name = ? WHERE id = ?",
            dto.name(), dto.id()
        );
    }

    @Override
    public void deleteUserActionType(UserActionTypesSyncDto dto) {
        jdbcTemplate.update("DELETE FROM user_action_types WHERE id = ?", dto.id());
    }
}