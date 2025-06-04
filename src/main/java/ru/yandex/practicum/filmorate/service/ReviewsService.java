package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.storage.activityLog.ActivityLogStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewsDBStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewsService {
    private final ReviewsDBStorage reviewsDBStorage;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final ActivityLogStorage activityLogStorage;


    public Reviews createReview(Reviews review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        if (reviewsDBStorage.searchReviewForUserIdAndFilmId(review.getUserId(), review.getFilmId())) {
            throw new NotFoundException("такой ревью уже существует");
        }
        review.setUseful(0);
        reviewsDBStorage.addRew(review);
        activityLogStorage.addActivity(review.getUserId(), "REVIEW", "ADD", review.getReviewId());
        return review;
    }

    public Reviews updateReview(Reviews review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        Reviews existing = reviewsDBStorage.getRewById(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        reviewsDBStorage.updateRew(existing);
        activityLogStorage.addActivity(existing.getUserId(), "REVIEW", "UPDATE", existing.getReviewId());
        return existing;
    }

    public void deleteReview(int id) {
        Reviews review = reviewsDBStorage.getRewById(id);
        activityLogStorage.addActivity(review.getUserId(), "REVIEW", "REMOVE", id);
        reviewsDBStorage.delRewById(review);
    }

    public Reviews getReviewById(int id) {
        return reviewsDBStorage.getRewById(id);
    }

    public List<Reviews> getPopularReviews(Integer filmId, int count) {
        if (filmId != null) {
            filmDbStorage.getFilmById(filmId);
            return reviewsDBStorage.getReviewsByFilmId(filmId)
                    .stream()
                    .sorted(Comparator.comparingInt(Reviews::getUseful).reversed())
                    .limit(count)
                    .collect(Collectors.toList());
        }

        return reviewsDBStorage.getRew()
                .stream()
                .sorted(Comparator.comparingInt(Reviews::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(int reviewId, int userId) {
        userDbStorage.getUserById(userId);
        reviewsDBStorage.addUseful(reviewId, userId);
    }

    public void removeLikeAndDislike(int reviewId, int userId) {
        userDbStorage.getUserById(userId);
        reviewsDBStorage.deleteLikeForReview(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        userDbStorage.getUserById(userId);
        reviewsDBStorage.delUseful(reviewId, userId);
    }

    private void validateUserAndFilm(int userId, int filmId) {
        userDbStorage.getUserById(userId);
        filmDbStorage.getFilmById(filmId);
    }
}