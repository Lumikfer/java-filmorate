package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;
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
            throw new ValidationException("Ошибка добавления");
        }
        film.setId(id++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsValue(film) || film == null || film.getReleaseDate().isBefore(targetDate)) {
            throw new ValidationException("такого фильма нет");
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
