package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Reviews;
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

    public Reviews createReview(Reviews review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        review.setUseful(0);
        reviewsDBStorage.addRew(review);
        return review;
    }

    public Reviews updateReview(Reviews review) {
        validateReviewExists(review.getReviewId());
        Reviews existing = reviewsDBStorage.getRewById(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        reviewsDBStorage.updateRew(existing);
        return existing;
    }

    public void deleteReview(int id) {
        Reviews review = reviewsDBStorage.getRewById(id);
        if (review == null) {
            throw new NotFoundException("Review not found");
        }
        reviewsDBStorage.delRewById(review);
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
        validateReviewAndUser(reviewId, userId);
        reviewsDBStorage.addUseful(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        validateReviewAndUser(reviewId, userId);
        reviewsDBStorage.delUseful(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        validateReviewAndUser(reviewId, userId);
        reviewsDBStorage.delUseful(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        validateReviewAndUser(reviewId, userId);
        addLike(reviewId, userId);
    }

    private void validateUserAndFilm(int userId, int filmId) {
        if (userDbStorage.getUserById(userId) == null) {
            throw new ValidationException("User not found");
        }
        if (filmDbStorage.getFilmById(filmId) == null) {
            throw new ValidationException("Film not found");
        }
    }

    private void validateReviewExists(int id) {
        if (reviewsDBStorage.getRewById(id) == null) {
            throw new NotFoundException("Review not found");
        }
    }

    private void validateReviewAndUser(int reviewId, int userId) {
        validateReviewExists(reviewId);
        if (userDbStorage.getUserById(userId) == null) {
            throw new NotFoundException("User not found");
        }
    }
}