package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Film addFilm(Film film) {

        Mpa mpa = mpaStorage.getMpaById(film.getMpa().getId());
        if (mpa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MPA not found");
        }
        film.setMpa(mpa);

        Set<Genre> validatedGenres = new HashSet<>();
        for (Genre genre : film.getGenres()) {
            Genre existing = genreStorage.getGenreById(genre.getId());
            if (existing == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found: " + genre.getId());
            }
            validatedGenres.add(existing);
        }
        film.setGenres(new ArrayList<>(validatedGenres));

        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
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

    public void addLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        film.getLike().add(userId);
    }

    public void deleteLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);

    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLike().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film getFilmOrThrow(int id) {
        return filmStorage.getFilmById(id);

    }


    private void getUserOrThrow(int id) {
        try {
            userStorage.getUserById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }
}