package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Set;

public interface GenreStorage {

    Collection<Genre> getAllGenres();

    Genre getGenreById(Integer id);

    Set<Genre> getFilmGenres(int filmId);
}
