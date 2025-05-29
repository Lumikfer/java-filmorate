package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Reviews;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
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
        String sql = "SELECT * FROM reviews WHERE reviewId = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
    }

    @Override
    public void addRew(Reviews reviews) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"review_id"});
            stmt.setString(1, reviews.getContent());
            stmt.setBoolean(2, reviews.getIsPositive());
            stmt.setInt(3, reviews.getUserId());
            stmt.setInt(4, reviews.getFilmId());
            stmt.setInt(5, 0);
            return stmt;
        }, keyHolder);

        reviews.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    public void delRewById(Reviews reviews) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, reviews.getId());
    }

    @Override
    public void addUseful(int rewid, int userid) {
        String likeSql = "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(likeSql, rewid, userid);

        String updateSql = "UPDATE reviews SET useful = useful + 1 WHERE reviewId = ?";
        jdbcTemplate.update(updateSql, rewid);
    }

    @Override
    public void delUseful(int rewid, int userid) {

        String likeSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(likeSql, rewid, userid);

        String updateSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(updateSql, rewid);
    }

    @Override
    public void updateRew(Reviews reviews) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql,
                reviews.getContent(),
                reviews.getIsPositive(),
                reviews.getId());
    }

    @Override
    public List<Reviews> getReviewsByFilmId(int filmId) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId);
    }

    private Reviews mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Reviews.builder()
                .id(rs.getInt("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .useFul(rs.getInt("useful"))
                .build();
    }
}