package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.model.Director;

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

    public List<Film> getPopularFilms(int count, Integer year, Integer genreId) {
        return filmStorage.getPopularFilms(count, year, genreId);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release date must be after 1895-12-28");
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


    private User getUserOrThrow(int id) {
        try {
            return userStorage.getUserById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }


    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
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

        String queryLower = query.toLowerCase();
        Collection<Film> allFilms = filmStorage.getFilms();
        boolean searchByTitle = by.contains("title");
        boolean searchByDirector = by.contains("director");
        return allFilms.stream()
                .filter(film -> {

                    if (searchByTitle && film.getName() != null) {
                        if (film.getName().toLowerCase().contains(queryLower)) {
                            return true;
                        }
                    }

                    if (searchByDirector && film.getDirectors() != null) {
                        return film.getDirectors().stream()
                                .filter(Objects::nonNull)
                                .map(Director::getName)
                                .filter(Objects::nonNull)
                                .anyMatch(directorName ->
                                        directorName.toLowerCase().contains(queryLower)
                                );
                    }

                    return false;
                })
                .sorted(Comparator.comparingInt((Film film) ->
                        film.getLike() != null ? -film.getLike().size() : 0
                ))
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        log.debug("Получение фильмов режисера с ID: {} с сортировкой по '{}'", directorId, sortBy);
        return filmStorage.getFilmsByDirectorId(directorId, sortBy);
    }
}