package ru.yandex.practicum.filmorate.storage.activityLog;

import ru.yandex.practicum.filmorate.model.ActivityLog;

import java.util.List;

public interface ActivityLogStorage {

    void addActivity(int userId, String actionType, String operation, int targetId);

    List<ActivityLog> getFeedForUser(int userId);

}