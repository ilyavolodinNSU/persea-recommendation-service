package ru.persea.recommendationservice.service;

import ru.persea.recommendationservice.handler.dto.UserActionsSyncDto;
import ru.persea.recommendationservice.handler.dto.UserActionTypesSyncDto;

public interface UserActionService {

    void insertUserAction(UserActionsSyncDto dto);

    void updateUserAction(UserActionsSyncDto dto);

    void deleteUserAction(UserActionsSyncDto dto);

    void insertUserActionType(UserActionTypesSyncDto dto);

    void updateUserActionType(UserActionTypesSyncDto dto);

    void deleteUserActionType(UserActionTypesSyncDto dto);
}