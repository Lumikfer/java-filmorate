package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {

    List<Genre> getAllGenres();

    Genre getGenreById(Integer id);

    List<Genre> getFilmGenres(int filmId);

    void insertInFilmGenreTable(Film film);
}