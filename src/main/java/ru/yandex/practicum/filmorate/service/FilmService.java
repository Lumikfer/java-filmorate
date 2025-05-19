package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private  LocalDate targetDate = LocalDate.of(1895, 12, 28);

    public Film addFilm(Film film) {
        validateFilm(film);
        Integer mpaId = film.getMpa().getId();
        if (mpaStorage.getMpaById(mpaId) == null) {
            throw new NotFoundException("Mpa not found");
        }

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
        validateFilm(film);
        Film film1 = getFilmOrThrow(film.getId());
        filmStorage.updateFilm(film);
        return film1;
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
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmStorage.removeLike(filmId, userId);
    }

    private Film getFilmOrThrow(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Film not found with id: " + id);
        }
        return film;
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(targetDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release date must be after 1895-12-28");
        }
    }


    private User getUserOrThrow(int id) {
        try {
          return   userStorage.getUserById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }
}