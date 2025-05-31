package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.storage.ActivityLogStorage;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.ReviewsDBStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

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
        validateReviewExists(review.getReviewId());
        Reviews existing = reviewsDBStorage.getRewById(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        reviewsDBStorage.updateRew(existing);
        activityLogStorage.addActivity(review.getUserId(), "REVIEW", "UPDATE", review.getReviewId());
        return existing;
    }

    public void deleteReview(int id) {
        Reviews review = reviewsDBStorage.getRewById(id);
        reviewsDBStorage.delRewById(review);
        activityLogStorage.addActivity(review.getUserId(), "REVIEW", "REMOVE", review.getReviewId());
    }

    public Reviews getReviewById(int id) {
        Reviews review = reviewsDBStorage.getRewById(id);
        if (review == null) {
            throw new NotFoundException("Review not found");
        }
        return review;
    }

    public List<Reviews> getPopularReviews(Integer filmId, int count) {
        if (filmId != null) {
            if (filmDbStorage.getFilmById(filmId) == null) {
                throw new NotFoundException("Film not found");
            }
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
        activityLogStorage.addActivity(userId, "LIKE", "ADD", reviewId);
    }

    public void removeLikeAndDislike(int reviewId, int userId) {
        userDbStorage.getUserById(userId);
        reviewsDBStorage.deleteLikeForReview(reviewId, userId);
        activityLogStorage.addActivity(userId, "LIKE", "REMOVE", reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        userDbStorage.getUserById(userId);
        reviewsDBStorage.delUseful(reviewId, userId);
        activityLogStorage.addActivity(userId, "LIKE", "ADD", reviewId);
    }

    private void validateUserAndFilm(int userId, int filmId) {
        userDbStorage.getUserById(userId);
        filmDbStorage.getFilmById(filmId);
    }

    private void validateReviewExists(int id) {
        if (reviewsDBStorage.getRewById(id) == null) {
            throw new NotFoundException("Review not found");
        }
    }

}