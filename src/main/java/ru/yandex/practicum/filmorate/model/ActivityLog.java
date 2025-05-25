package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityLog {

    private int activityId;
    private int userId;
    private String actionType;
    private String operation;
    private int targetId;
    private LocalDateTime created;


    public ActivityLog(int activityId, int userId, String actionType, String operation, int targetId, LocalDateTime created) {
        this.activityId = activityId;
        this.userId = userId;
        this.actionType = actionType;
        this.operation = operation;
        this.targetId = targetId;
        this.created = created;
    }

}