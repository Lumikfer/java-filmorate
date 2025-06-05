package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getFilms();

    Film getFilmById(int id);

    void deleteFilmById(int id);

    void addRating(int filmId, int userId, Double rating);

    void removeLike(int filmId, int userId);

    List<Film> getPopularFilms(int count, Integer year, Integer genreId);

    List<Film> getFilmsByDirectorId(int directorId, String sortBy);

    Boolean chekLikeForFilm(int filmId, int userId);

    List<Film> searchFilmsByQuery(String query, String by);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getRecomendationFilm(int userId);
}

