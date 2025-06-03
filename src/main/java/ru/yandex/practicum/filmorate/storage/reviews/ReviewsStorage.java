package ru.yandex.practicum.filmorate.storage.reviews;

import ru.yandex.practicum.filmorate.model.Reviews;

import java.util.List;

public interface ReviewsStorage {
    List<Reviews> getRew();

    Reviews getRewById(int id);

    void addRew(Reviews review);

    void delRewById(Reviews review);

    void addUseful(int reviewId, int userId);

    void delUseful(int reviewId, int userId);

    void updateRew(Reviews review);

    List<Reviews> getReviewsByFilmId(int filmId);
}