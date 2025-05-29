package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Reviews;

import java.util.List;

public interface ReviewsStorage {

    List<Reviews> getRew();

    Reviews getRewById(int id);

    void addRew(Reviews reviews);

    void delRewById(Reviews reviews);

    void addUseful(int rewid,int userid);

    void delUseful(int rewid,int userid);

    void updateRew(Reviews reviews);

    List<Reviews> getReviewsByFilmId(int filmId);



}
