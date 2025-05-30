package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
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
        try {
            Reviews review = jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
            if (review != null) {
                review.setUseful(getUsefulCount(id));
            }
            return review;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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
        String deleteReactionsSql = "DELETE FROM review_ratings WHERE review_id = ?";
        jdbcTemplate.update(deleteReactionsSql, review.getReviewId());

        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getReviewId());
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
        String sql = "SELECT * FROM reviews WHERE film_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId);
    }

    public void addReaction(int reviewId, int userId, boolean isLike) {
        String sql = "INSERT INTO review_ratings (review_id, user_id, is_positive) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (review_id, user_id) DO UPDATE SET is_positive = EXCLUDED.is_positive";

        jdbcTemplate.update(sql, reviewId, userId, isLike);
        updateUsefulCount(reviewId);
    }

    public void removeReaction(int reviewId, int userId) {
        String sql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUsefulCount(reviewId);
    }

    public int getUsefulCount(int reviewId) {
        String sql = "SELECT COALESCE(SUM(CASE WHEN is_positive THEN 1 ELSE -1 END), 0) " +
                "FROM review_ratings WHERE review_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
    }

    private void updateUsefulCount(int reviewId) {
        String sql = "UPDATE reviews SET useful = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, getUsefulCount(reviewId), reviewId);
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