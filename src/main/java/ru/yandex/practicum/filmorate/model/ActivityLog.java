package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityLog {

    private int activityId;
    @NotNull
    private int userId;
    @NotNull
    private String actionType;
    @NotNull
    private String operation;
    @NotNull
    private int targetId;
    @NotNull
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