package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ReviewsDBStorage implements ReviewsStorage {
    private final JdbcTemplate jdbcTemplate;


    @Override
    public List<Reviews> getRew() {
        String sql = "SELECT * FROM reviews";
        return jdbcTemplate.query(sql, this::mapRowToReview);
    }

    @Override
    public Reviews getRewById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
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
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getReviewId());
    }

    @Override
    public void addUseful(int reviewId, int userId) {
        Integer currentReaction = getUserReaction(reviewId, userId);

        if (currentReaction == null) {

            addOrUpdateReaction(reviewId, userId, 1);
            updateReviewUseful(reviewId, 1);
        } else if (currentReaction == 0) {

            addOrUpdateReaction(reviewId, userId, 1);
            updateReviewUseful(reviewId, 2);
        }
    }

    @Override
    public void delUseful(int reviewId, int userId) {
        Integer currentReaction = getUserReaction(reviewId, userId);

        if (currentReaction == null) {

            addOrUpdateReaction(reviewId, userId, 0);
            updateReviewUseful(reviewId, -1);
        } else if (currentReaction == 1) {

            addOrUpdateReaction(reviewId, userId, 0);
            updateReviewUseful(reviewId, -2);
        }

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

    private void addOrUpdateReaction(int reviewId, int userId, int useful) {
        String updateSql = "UPDATE review_likes SET useful = ? WHERE review_id = ? AND user_id = ?";
        int updated = jdbcTemplate.update(updateSql, useful, reviewId, userId);

        if (updated == 0) {
            String insertSql = "INSERT INTO review_likes (review_id, user_id, useful) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, reviewId, userId, useful);
        }
    }

    private void updateReviewUseful(int reviewId, int delta) {
        String sql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(sql, delta, reviewId);
    }

    public Integer getUserReaction(int reviewId, int userId) {
        String sql = "SELECT useful FROM review_likes WHERE review_id = ? AND user_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


}