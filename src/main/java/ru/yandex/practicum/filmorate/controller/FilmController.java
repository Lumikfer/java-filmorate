package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    Map<Integer, Film> films = new HashMap<>();
    private static int filmId = 1;
    private final LocalDate targetDate = LocalDate.of(1895, 12, 29);


    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {

       valid(film);
        film.setId(filmId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен, id фильма: " + film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        valid(film);
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с данным  id  не найден", film.getId());
            throw new ValidationException("Фильм не найден");
        }
        films.put(film.getId(), film);
        log.info("Фильм обновлен, id: " + film.getId());

        return film;
    }

    @GetMapping
    public List<Film> getFilms() throws IOException {
        List<Film> film = new ArrayList<>(films.values());
        if (film.isEmpty()) {
            return new ArrayList<>();
        }
        return film;
    }

    private void valid(Film film) {
        if (film.getReleaseDate().isBefore(targetDate)) {
            throw new ValidationException("Дата выхода похже 1895");

        }
    }

}
