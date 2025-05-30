package ru.yandex.practicum.filmorate.model;

import lombok.Data;


@Data
public class ActivityLog {

    private int eventId;
    private int userId;
    private String eventType;
    private String operation;
    private int entityId;
    private Long timestamp;


    public ActivityLog(int eventId, int userId, String eventType, String operation, int entityId, Long timestamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timestamp = timestamp;
    }

}