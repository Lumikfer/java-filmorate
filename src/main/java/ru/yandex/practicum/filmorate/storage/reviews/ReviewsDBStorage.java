package ru.yandex.practicum.filmorate.storage.reviews;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
    private static final int ADD_USEFUL = 1;
    private static final int REMOVE_USEFUL = -1;
    private static final int EMPTY_USEFUL = 0;


    @Override
    public List<Reviews> getRew() {
        String sql = "SELECT * FROM reviews";
        return jdbcTemplate.query(sql, this::mapRowToReview);
    }

    @Override
    public Reviews getRewById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Reviews> reviews = jdbcTemplate.query(sql, this::mapRowToReview, id);
        if (reviews.isEmpty()) {
            throw new NotFoundException("не найден");
        }
        return reviews.getFirst();
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
        String sqlDel = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlDel, reviewId, userId);

        String sql = "INSERT INTO review_likes (review_id, user_id, useful) " +
                "VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, reviewId, userId, ADD_USEFUL);
        checkUsefulCountInReviewId(reviewId);
    }

    @Override
    public void delUseful(int reviewId, int userId) {
        String sqlDel = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlDel, reviewId, userId);

        String sql = "INSERT INTO review_likes (review_id, user_id, useful) " +
                "VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, reviewId, userId, REMOVE_USEFUL);
        checkUsefulCountInReviewId(reviewId);
    }

    public void deleteLikeForReview(int reviewId, int userId) {
        String sqlDel = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlDel, reviewId, userId);
        checkUsefulCountInReviewId(reviewId);
    }

    @Override
    public void updateRew(Reviews review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
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

    private void checkUsefulCountInReviewId(int reviewId) {
        String sqlUseful = "SELECT SUM(review_likes.useful) AS likes_count " +
                "FROM review_likes " +
                "WHERE review_id = ? " +
                "GROUP BY review_id";

        List<Integer> likesCounts = jdbcTemplate.queryForList(sqlUseful, Integer.class, reviewId);
        Integer likesCount;

        if (likesCounts.isEmpty()) {
            likesCount = EMPTY_USEFUL;
        } else {
            likesCount = likesCounts.getFirst();
        }

        String sql = "UPDATE reviews SET useful = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, likesCount, reviewId);
    }

    public Boolean searchReviewForUserIdAndFilmId(int userId, int filmId) {
        String sql = "SELECT * FROM reviews WHERE user_id = ? AND film_id = ?";
        List<Reviews> reviews = jdbcTemplate.query(sql, this::mapRowToReview, userId, filmId);
        return !reviews.isEmpty();
    }
}