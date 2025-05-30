package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Reviews;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
public class ReviewsDBStorage implements ReviewsStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewsDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reviews> getRew() {
        String sql = "SELECT * FROM reviews";
        return jdbcTemplate.query(sql, this::mapRowToReview);
    }

    @Override
    public Reviews getRewById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
    }

    @Override
    public void addRew(Reviews review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    public void delRewById(Reviews review) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getReviewId());
    }

    @Override
    public void addUseful(int reviewId, int userId) {

        String updateSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?;";
        jdbcTemplate.update(updateSql, reviewId);

    }

    @Override
    public void delUseful(int reviewId, int userId) {

        String updateSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(updateSql, reviewId);

    }

    @Override
    public void updateRew(Reviews review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
    }

    @Override
    public List<Reviews> getReviewsByFilmId(int filmId) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId);
    }

    private Reviews mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Reviews.builder()
                .reviewId(rs.getInt("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}