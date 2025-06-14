package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.activityLog.ActivityLogStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private static final int GENRE_MIN = 1;
    private static final int GENRE_MAX = 6;
    private static final int MPA_MIN = 1;
    private static final int MPA_MAX = 6;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final ActivityLogStorage activityLogStorage;
    private final DirectorStorage directorStorage;


    public Film addFilm(Film film) {
        validateFilm(film);

        if (film.getName() == null || film.getReleaseDate() == null) {
            throw new NotFoundException("Mpa ore ReleaseDate not found");
        }

        validateFilm(film);
        Film film1 = filmStorage.addFilm(film);
        genreStorage.insertInFilmGenreTable(film);
        directorStorage.insertInFilmDirectorTable(film);
        return film1;
    }

    public Film updateFilm(Film film) {
        filmStorage.getFilmById(film.getId());
        validateFilm(film);
        film = filmStorage.updateFilm(film);
        genreStorage.insertInFilmGenreTable(film);
        directorStorage.insertInFilmDirectorTable(film);
        film.setGenres(genreStorage.getFilmGenres(film.getId()));
        film.setDirectors(directorStorage.getDirectorsByFilmId(film.getId()));
        return film;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    public void deleteFilmById(int id) {
        filmStorage.getFilmById(id);
        filmStorage.deleteFilmById(id);
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        activityLogStorage.addActivity(userId, "LIKE", "ADD", filmId);

        if (filmStorage.checkLikeForFilm(filmId, userId)) {
            return;
        }

        filmStorage.addLike(filmId, userId);
        film.getLike().add(userId);
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        activityLogStorage.addActivity(userId, "LIKE", "REMOVE", filmId);

        if (!filmStorage.checkLikeForFilm(filmId, userId)) {
            return;
        }
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count, Integer year, Integer genreId) {
        return filmStorage.getPopularFilms(count, year, genreId);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release date must be after 1895-12-28");
            }
        }
        if (film.getMpa() != null) {
            if (film.getMpa().getId() > MPA_MAX || film.getMpa().getId() < MPA_MIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "МПА не может быть меньше " + MPA_MIN +
                        " и больше " + MPA_MAX);
            }
        }
        if (film.getGenres() != null) {
            List<Genre> genres = film.getGenres();
            for (Genre genre : genres) {
                if (genre.getId() > GENRE_MAX || genre.getId() < GENRE_MIN) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не верный id Genre");
                }
            }
        }
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> allFilms = new ArrayList<>(filmStorage.getFilms());
        List<Film> commonFilms = new ArrayList<>();

        for (Film film : allFilms) {
            Set<Integer> likes = film.getLike();
            if (likes != null) {
                if (likes.contains(userId) && likes.contains(friendId)) {
                    commonFilms.add(film);
                }
            }
        }

        return commonFilms.stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLike().size()).reversed())
                .collect(Collectors.toList());
    }

    public List<Film> searchFilmsByQuery(String query, String by) {
        String normalizedBy = by.trim().toLowerCase();
        List<String> validCriteria = List.of(Search.title.toString(), Search.director.toString());
        if (!Arrays.stream(normalizedBy.split(","))
                .anyMatch(validCriteria::contains)) {
            throw new ValidationException("Invalid search criteria. Use 'title', 'director' or both");
        }
        if (by.contains(Search.title.toString()) && by.contains(Search.director.toString())) {
            by = "all";
        } else {
            by = normalizedBy;
        }

        return filmStorage.searchFilmsByQuery(query, by);
    }

    public List<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        String orderByClause = switch (sortBy.toLowerCase()) {
            case "year" -> "f.release_date";
            case "likes" -> "like_count DESC";
            default -> throw new IllegalArgumentException("Неподдерживаемый параметр сортировки: " + sortBy);
        };

        log.debug("Получение фильмов режисера с ID: {} с сортировкой по '{}'", directorId, sortBy);
        List<Film> films = filmStorage.getFilmsByDirectorId(directorId, orderByClause);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильмы не найдены");
        }
        return films;
    }
}