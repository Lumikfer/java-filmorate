package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.ActivityLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ActivityLogDbStorage implements ActivityLogStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addActivity(int userId, String actionType, String operation, int targetId) {
        log.info("Добавление нового события user_id={}, action_type={}, operation={}, target_id={}",
                userId, actionType, operation, targetId);

        String sql = "INSERT INTO activity_log (user_id, action_type, operation, target_id, created) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                userId,
                actionType,
                operation,
                targetId,
                LocalDateTime.now()
        );
    }

    @Override
    public List<ActivityLog> getFeedForUser(int userId) {
        log.debug("Получение ленты событий для пользователя с ID: {}", userId);

        String sql = "SELECT activity_id, user_id, action_type, operation, target_id, created " +
                "FROM activity_log " +
                "WHERE user_id = ? " +
                "ORDER BY activity_id DESC";

        return jdbcTemplate.query(sql, this::mapRowToActivityLog, userId);
    }

    private ActivityLog mapRowToActivityLog(ResultSet rs, int rowNum) throws SQLException {
        return new ActivityLog(
                rs.getInt("activity_id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("operation"),
                rs.getInt("target_id"),
                rs.getTimestamp("created").toLocalDateTime()
        );
    }
}
