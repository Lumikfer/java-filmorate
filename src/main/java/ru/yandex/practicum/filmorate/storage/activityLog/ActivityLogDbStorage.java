package ru.yandex.practicum.filmorate.storage.activityLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.ActivityLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ActivityLogDbStorage implements ActivityLogStorage {
    private final JdbcTemplate jdbcTemplate;


    @Override
    public void addActivity(int userId, String eventType, String operation, int entityId) {
        log.info("Добавление нового события user_id={}, event_type={}, operation={}, entity_id={}",
                userId, eventType, operation, entityId);

        String sql = "INSERT INTO activity_log (user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                userId,
                eventType,
                operation,
                entityId,
                LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()

        );
    }

    @Override
    public List<ActivityLog> getFeedForUser(int userId) {
        log.debug("Получение ленты событий для пользователя с ID: {}", userId);

        String sql = "SELECT event_id, user_id, event_type, operation, entity_id, timestamp " +
                "FROM activity_log " +
                "WHERE user_id = ? " +
                "ORDER BY event_id";

        return jdbcTemplate.query(sql, this::mapRowToActivityLog, userId);
    }

    private ActivityLog mapRowToActivityLog(ResultSet rs, int rowNum) throws SQLException {
        return new ActivityLog(
                rs.getInt("event_id"),
                rs.getInt("user_id"),
                rs.getString("event_type"),
                rs.getString("operation"),
                rs.getInt("entity_id"),
                rs.getLong("timestamp")
        );
    }
}
