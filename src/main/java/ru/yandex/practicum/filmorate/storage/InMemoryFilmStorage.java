package ru.yandex.practicum.filmorate.storage;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    Map<Integer, Film> films = new HashMap<>();
    private static int id = 1;
    private final LocalDate targetDate = LocalDate.of(1895, 12, 29);

    @Override
    public Film addFilm(Film film) {

        if (films.containsValue(film) || film == null || film.getReleaseDate().isBefore(targetDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        film.setId(id++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsValue(film) || film == null || film.getReleaseDate().isBefore(targetDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }

    @Override
    public void deleteFilmById(int id) {
        films.remove(id);

    }

}
