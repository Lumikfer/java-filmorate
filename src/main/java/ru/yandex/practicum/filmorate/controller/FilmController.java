package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {

        return filmService.updateFilm(film);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id,
                        @PathVariable int userId) {
        int rating = 1;
        filmService.addLike(id, userId, rating);
    }

    @PutMapping("/{id}/like/{userId}/{rating}")
    public void addRating(@PathVariable int id,
                          @PathVariable int userId,
                          @PathVariable int rating) {
        filmService.addLike(id, userId, rating);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilmByGenre(@RequestParam(defaultValue = "10") int count,
                                            @RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer genreId) {
        return filmService.getPopularFilms(count, year, genreId);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable int id) {
        filmService.deleteFilmById(id);
    }

    /*
    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId,
                                     @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query,
                                  @RequestParam(defaultValue = "title,director") String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Query parameter cannot be empty");
        }

        String normalizedBy = by.trim().toLowerCase();
        Set<String> validCriteria = Set.of("title", "director");

        if (!Arrays.stream(normalizedBy.split(","))
                .anyMatch(validCriteria::contains)) {
            throw new ValidationException("Invalid search criteria. Use 'title', 'director' or both");
        }

        return filmService.searchFilms(query, normalizedBy);
    }

     */

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable int directorId,
                                         @RequestParam(name = "sortBy", defaultValue = "year") String sortBy) {
        return filmService.getFilmsByDirectorId(directorId, sortBy);
    }
}