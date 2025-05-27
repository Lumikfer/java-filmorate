package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;


    public Film addFilm(Film film) {
        validateFilm(film);
        Integer mpaId = film.getMpa().getId();
        if (mpaStorage.getMpaById(mpaId) == null) {
            throw new NotFoundException("Mpa not found");
        }
        Set<Genre> validatedGenres = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            validatedGenres.add(genreStorage.getGenreById(genre.getId()));
        }
        film.setGenres(new ArrayList<>(validatedGenres));
        Set<Director> validateDirector = new LinkedHashSet<>();
        for (Director director : film.getDirector()) {
            validateDirector.add(directorStorage.getDirectorById(director.getId()));
        }
        film.setDirector(new ArrayList<>(validateDirector));
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        Film film1 = getFilmOrThrow(film.getId());
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
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        log.info(userId + " поставил лайк " + filmId);
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

    public List<Film> getPopularFilms(int count, int year, int genreId) {
        return filmStorage.getPopularFilms(count, year, genreId);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release date must be after 1895-12-28");
        }
    }

    private User getUserOrThrow(int id) {
        try {
            return userStorage.getUserById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }


    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> commonFilms = new ArrayList<>();
        List<Film> allfilm = new ArrayList<>(filmStorage.getFilms());
        for (Film film : allfilm) {
            if (film.getLike().contains(userId) && film.getLike().contains(friendId)) {
                commonFilms.add(film);
            }
        }
        List<Film> sortedFilms = commonFilms.stream()
                .sorted(Comparator.comparing(film -> film.countLike(film.getLike())))
                .collect(Collectors.toList());

        return commonFilms;
    }

    public List<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        log.debug("Получение фильмов режисера с ID: {} с сортировкой по '{}'", directorId, sortBy);
        return filmStorage.getFilmsByDirectorId(directorId, sortBy);
    }
}