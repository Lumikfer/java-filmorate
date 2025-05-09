package ru.yandex.practicum.filmorate.storage;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 1;
    private final LocalDate targetDate = LocalDate.of(1895, 12, 28);

    @Override
    public Film addFilm(Film film) {
        validateFilm(film);
        film.setId(id++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        validateFilm(film);
        films.put(film.getId(), film);
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(targetDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release date must be after 1895-12-28");
        }
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(int id) {
        if (!films.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
        return films.get(id);
    }

    @Override
    public void deleteFilmById(int id) {
        films.remove(id);
    }
}