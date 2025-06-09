package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface DirectorStorage {

    Director createDirector(Director director);

    Director getDirectorById(Integer id);

    Director updateDirector(Director director);

    void deleteDirectorById(Integer id);

    Collection<Director> getAllDirectors();

    List<Director> getDirectorsByFilmId(int filmId);

    void insertInFilmDirectorTable(Film film);
}