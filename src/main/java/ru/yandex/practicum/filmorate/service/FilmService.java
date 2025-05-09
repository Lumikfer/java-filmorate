package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;


    public Film addFilm(Film film) {

        if (filmStorage.getFilms().contains(film) || film == null) {
            throw new ValidationException("такой film уже есть ");
        }
        return filmStorage.addFilm(film);
    }


    public Film updateFilm(Film film) {
        if (!filmStorage.getFilms().contains(film) || film == null) {
            throw new ValidationException("такого фильма нет");
        }

        return filmStorage.updateFilm(film);
    }


    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }


    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }


    public void deleteFilmById(int id) {
        filmStorage.deleteFilmById(id);

    }

    public void filmLike(int userid, int filmid) {
        if (filmStorage.getFilmById(filmid) == null || userStorage.getUserById(userid) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film or user not found");
        }
        filmStorage.getFilmById(filmid).addLike(userid);

        log.info("фильму " + filmStorage.getFilmById(filmid).getName() + " поставил лайк " + userStorage.getUserById(userid).getName());
    }

    public void deleteLike(int userid, int filmid) {
        if (filmStorage.getFilmById(filmid) == null || userStorage.getUserById(userid) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film or user not found");
        }
        filmStorage.getFilmById(filmid).getLike().remove(userid);
        log.info("фильму " + filmStorage.getFilmById(filmid).getName() + " убрал лайк " + userStorage.getUserById(userid).getName());
    }

    public List<String> getlikesfilm(int id) {
        List<String> name = new ArrayList<>();
        for (int i : filmStorage.getFilmById(id).getLike()) {
            name.add(userStorage.getUserById(i).getName());

        }
        return name;
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> {
                    int likesCompare = Integer.compare(
                            f2.getLike().size(),
                            f1.getLike().size()
                    );

                    return likesCompare != 0
                            ? likesCompare
                            : f1.getName().compareTo(f2.getName());
                })
                .limit(count)
                .collect(Collectors.toList());
    }

}
