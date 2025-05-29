package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.ReviewsDBStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewsService {

    private final ReviewsDBStorage reviewsDBStorage;
    private final FilmDbStorage filmDbStorage;

    public void addRew(Reviews rew) {
        reviewsDBStorage.addRew(rew);
    }

    public void getRewById(int id) {
        reviewsDBStorage.getRewById(id);

    }

    public void getAllRew() {
        reviewsDBStorage.getRew();
    }

    public void setLike(int rewid,int userid) {
      Reviews rew =  reviewsDBStorage.getRewById(rewid);
        reviewsDBStorage.addUseful(rewid,userid);
    }

    public void delLike(int rewid,int userid) {
        reviewsDBStorage.delUseful(rewid,userid);
    }

    public void updateRew(Reviews rew) {
        reviewsDBStorage.updateRew(rew);
    }

    public List<Reviews> getPop(int count , Integer id) {
        if(id == null) {
           List<Film> films = filmDbStorage.getFilms().stream()
                   .collect(Collectors.toList());
           for(Film film:films) {
              List< List<Reviews>> rews = new ArrayList<>();
               for(int i = 0;i<count;i++) {
                   rews.add(reviewsDBStorage.getReviewsByFilmId(film.getId()));

               }
              List<Reviews> reviews = rews.stream()
                       .flatMap(List::stream)
                       .collect(Collectors.toList());
               return reviews;

           }

        }else {
            List<Reviews> rews = new ArrayList<>();
        for(int i = 0 ;i<count-1;i++) {
            rews.add(reviewsDBStorage.getReviewsByFilmId(id).get(i));
        }
            return rews;
        }

        return new ArrayList<>();
    }


}
