package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Reviews;

import java.util.List;

public interface ReviewsStorage {
    List<Reviews> getRew();

    Reviews getRewById(int id);

    void addRew(Reviews review);

    void delRewById(Reviews review);

    void updateRew(Reviews review);

    List<Reviews> getReviewsByFilmId(int filmId);

    // Новые методы для реакций
    void addReaction(int reviewId, int userId, boolean isLike);

    void removeReaction(int reviewId, int userId);
}